/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.inputstream

import cats.Applicative
import cats.implicits._
import cats.kernel.{Monoid, Semigroup}

import scala.language.higherKinds
import scala.reflect.ClassTag

trait InputStream[F[_], A] {
  def map[B: ClassTag](func: A => B): InputStream[F, B]
  def flatMap[B: ClassTag](func: A => TraversableOnce[B]): InputStream[F, B]
  def filter(func: A => Boolean): InputStream[F, A]
  def collect[B: ClassTag](func: PartialFunction[A, B]): InputStream[F, B]
  def inner: F[A]
  def mapInner[B: ClassTag](func: F[A] => F[B]): InputStream[F, B]
  def union(inputStream: InputStream[F, A]): InputStream[F, A]
  def union(inputStream: TraversableOnce[A]): InputStream[F, A]
}

object InputStream {
  implicit def inputStreamSemigroup[F[_], T]: Semigroup[InputStream[F, T]] = new Semigroup[InputStream[F, T]] {
    override def combine(x: InputStream[F, T], y: InputStream[F, T]): InputStream[F, T] = x.union(y)
  }

  implicit def inputStreamMonoid[F[_], T]: Monoid[Option[InputStream[F, T]]] = new Monoid[Option[InputStream[F, T]]] {
    override def empty: Option[InputStream[F, T]] = None

    override def combine(x: Option[InputStream[F, T]], y: Option[InputStream[F, T]]): Option[InputStream[F, T]] = {
      Applicative[Option].product(x, y)
        .map((Semigroup[InputStream[F, T]].combine _).tupled)
    }
  }
}