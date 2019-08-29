/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.graph

import org.dagobuh.api.appliers.{InletApplier, StreamletApplier}
import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds

sealed trait Vertex[+A, +B] {
}

case class InletVertex[F[_], G, I](streamlet: G)(implicit applier: InletApplier[F, G, I]) extends Vertex[Nothing, I] {
  def apply(): InputStream[F, I] = applier.run(streamlet)
}

case class StreamletVertex[F[_], G, H, I](streamlet: G)(implicit applier: StreamletApplier[F, G, H, I]) extends Vertex[H, I] {
  def apply(inputStream: InputStream[F, H]): InputStream[F, I] = applier.run(inputStream, streamlet)
}
//
//case class StreamletSplitter[F[_], G1, G2, H1, H2, I1, I2](streamlets: (G1, G2))(implicit firstApplier: StreamletApplier[F, G1, H1, I1], secondApplier: StreamletApplier[F, G2, H2, I2]) extends Vertex {
//  def apply(inputStream: InputStream[F, (H1, H2)]): (InputStream[F, I1], InputStream[F, I2]) = {
//    val (first, second) = streamlets
//    (firstApplier.run(inputStream.map(_._1), first),
//    secondApplier.run(inputStream.map(_._2), second))
//  }
//}
//
//case class StreamletFanIn[F[_], G, H, I](streamlet: G)(implicit applier: StreamletApplier[F, G, H, I]) extends Vertex {
//  def apply(inputStreams: NonEmptyList[InputStream[F, H]]): InputStream[F, I] = {
//    applier.run(inputStreams.reduce, streamlet)
//  }
//}
