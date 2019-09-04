package org.dagobuh.api.streamlets

import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds

sealed trait Streamlet[+A, +B] extends Serializable

trait Source[F[_], A] extends Streamlet[Nothing, A] {
  def run(): InputStream[F, A]
}
trait Transformer[F[_], A, B] extends Streamlet[A, B] {
  def run(input: InputStream[F, A]): InputStream[F, B]
}
trait Sink[F[_], A] extends Streamlet[A, Nothing] {
  def run(input: InputStream[F, A]): Unit
}
