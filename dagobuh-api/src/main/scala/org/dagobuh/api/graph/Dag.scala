/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.graph

import org.dagobuh.api.graph.Dag.EdgeMap
import org.dagobuh.api.inputstream.InputStream

import scala.collection.mutable

case class DagBuilder[A](current: Vertex[Any, A],
                            private val edges: mutable.ListBuffer[(Vertex[Any, Any], Vertex[Any, Any])] = mutable.ListBuffer.empty) {

  def ~>[B](next: Vertex[A, B]): DagBuilder[B] = {
    edges.append((current, next).asInstanceOf[(Vertex[Any, Any], Vertex[Any, Any])])
    DagBuilder(next, edges)
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

  private def validateDag(edges: List[(Vertex[Any, Any], Vertex[Any, Any])]): Unit = {
    //TODO: Use result types and make build() fallible
    val from = edges.map(_._1).toSet
    val to = edges.map(_._2).toSet
    val roots = from diff to
    val selfRefs = edges.filter { case (f, s) => f == s }
    if (selfRefs.nonEmpty) {
      throw new IllegalArgumentException(s"Invalid DAG: Self references are not allowed\nSelf Refs: $selfRefs")
    }
    if (roots.exists(!_.isInstanceOf[InletVertex[Any, Any, Any]])) {
      throw new IllegalArgumentException(s"Invalid DAG: All roots must be instances of InletVertex\nRoots: $roots")
    }
  }
}

case class Dag(private val root: Vertex[Any, Any], private val edges: EdgeMap, private val reverseEdges: EdgeMap) {
  def run(): Unit = {
    bottomUpExection(leaves(root), None, mutable.HashMap.empty)
  }
  private def leaves(root: Vertex[Any, Any]): List[Vertex[Any, Any]] = {
    val out = mutable.ListBuffer.empty[Vertex[Any, Any]]
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
  private def bottomUpExection(leaves: List[Vertex[Any, Any]], in: Option[InputStream[Any, Any]], seen: mutable.HashMap[Vertex[Any, Any], InputStream[Any, Any]]): List[InputStream[Any, Any]] = {
    val runVert: (Vertex[Any, Any], Option[InputStream[Any, Any]]) => InputStream[Any, Any] = (vert, in) => {
      val out = vert match {
        case v@StreamletVertex(_) =>
          v.apply(in.getOrElse(throw new IllegalArgumentException("Invalid DAG: Inlet must be the root node")))
        case v@InletVertex(_) =>
          v.apply()
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
              val unionedOutput = outs.reduce((x, y) => x.union(y))
              runVert(node, Some(unionedOutput))
            case None =>
              runVert(node, in)
          }
      }
    }
  }
}

object Dag {
  private type EdgeMap = Map[Vertex[Any, Any], List[Vertex[Any, Any]]]
}
