/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.graph

import cats.kernel.Monoid
import org.dagobuh.api.{DagBuilderUnionError, DagobuhError}
import org.dagobuh.api.graph.Dag.EdgeMap
import org.dagobuh.api.inputstream.InputStream
import org.dagobuh.api.inputstream.InputStream.inputStreamMonoid
import org.dagobuh.api.streamlets.{Sink, Source, Streamlet, Transformer}

import scala.collection.mutable

case class DagBuilder[+A](current: Streamlet[Any, A],
                            private val edges: mutable.ListBuffer[(Streamlet[Any, Any], Streamlet[Any, Any])] = mutable.ListBuffer.empty) {

  def ~>[U >: A, B](next: Streamlet[U, B]): DagBuilder[B] = {
    edges.append((current, next).asInstanceOf[(Streamlet[Any, Any], Streamlet[Any, Any])])
    DagBuilder(next.asInstanceOf[Streamlet[Any, B]], edges)
  }

  def |[U >: A](other: DagBuilder[U]): Either[DagobuhError, DagBuilder[U]] = {
    if (current == other.current) {
      Right(DagBuilder(current, edges ++ other.edges))
    } else {
      Left(DagBuilderUnionError("Cannot union DagBuilders unless the current node is the same for both"))
    }
  }

  def build(): List[Dag] = {
    val from = edges.map(_._1).toSet
    val to = edges.map(_._2).toSet
    val roots = from diff to
    validateDag(edges.toList)
    val edgeMap = edges.toList.groupBy(_._1).mapValues(_.map(_._2))
    val reverseEdgeMap = edges.toList.groupBy(_._2).mapValues(_.map(_._1))
    roots.map(Dag(_, edgeMap, reverseEdgeMap)).toList
  }

  private def validateDag(edges: List[(Streamlet[_, _], Streamlet[_, _])]): Unit = {
    //TODO: Use result types and make build() fallible
    val from = edges.map(_._1).toSet
    val to = edges.map(_._2).toSet
    val roots = from diff to
    val selfRefs = edges.filter { case (f, s) => f == s }
    if (selfRefs.nonEmpty) {
      throw new IllegalArgumentException(s"Invalid DAG: Self references are not allowed\nSelf Refs: $selfRefs")
    }
    if (roots.exists(!_.isInstanceOf[Source[Any, Any]])) {
      throw new IllegalArgumentException(s"Invalid DAG: All roots must be instances of InletStreamlet\nRoots: $roots")
    }
  }
}

case class Dag(private val root: Streamlet[Any, Any], private val edges: EdgeMap, private val reverseEdges: EdgeMap) {
  def run(): Unit = {
    bottomUpExection(leaves(root), None, mutable.HashMap.empty)
  }
  private def leaves(root: Streamlet[Any, Any]): List[Streamlet[Any, Any]] = {
    val out = mutable.ListBuffer.empty[Streamlet[Any, Any]]
    val verts = mutable.Queue(root)
    while (verts.nonEmpty) {
      val node = verts.dequeue()
      edges.get(node) match {
        case Some(children) => verts.enqueue(children:_*)
        case None => out.append(node)
      }
    }
    out.toList
  }

  /**
    * Despite what it may seem, seen is _NOT_
    * purely an optimization for already seen nodes.
    *
    * With streaming frameworks like Flink, applying the same
    * node multiple times would create internal Flink DAGs that duplicate work
    * and if they are stateful could potentially lose data.
    *
    * @param leaves leave nodes of DAG
    * @param in input stream
    * @param seen seen vertices
    * @return
    */
  private def bottomUpExection(leaves: List[Streamlet[Any, Any]], in: Option[InputStream[Any, Any]], seen: mutable.HashMap[Streamlet[Any, Any], Option[InputStream[Any, Any]]]): List[Option[InputStream[Any, Any]]] = {
    val runVert: (Streamlet[Any, Any], Option[InputStream[Any, Any]]) => Option[InputStream[Any, Any]] = (vert, in) => {
      val out = (vert: Streamlet[_, _]) match {
        case value: Source[_, _] =>
          Some(value.asInstanceOf[Source[Any, Any]].run())
        case value: Transformer[_, _, _] =>
          Some(value.asInstanceOf[Transformer[Any, Any, Any]].run(in.getOrElse(throw new IllegalArgumentException(s"Invalid DAG: No input for vertex $value"))))
        case value: Sink[_, _] =>
          value.asInstanceOf[Sink[Any, Any]].run(in.getOrElse(throw new IllegalArgumentException(s"Invalid DAG: No input for vertex $value")))
          None
      }
      seen(vert) = out
      out
    }
    leaves.map { node =>
      seen.get(node) match {
        case Some(out) => out
        case None =>
          reverseEdges.get(node) match {
            case Some(parents) =>
              val outs = bottomUpExection(parents, in, seen)
              val unionedOutput = Monoid.combineAll(outs)(inputStreamMonoid[Any, Any])
              runVert(node, unionedOutput)
            case None =>
              runVert(node, in)
          }
      }
    }
  }
}

object Dag {
  private type EdgeMap = Map[Streamlet[Any, Any], List[Streamlet[Any, Any]]]
}
