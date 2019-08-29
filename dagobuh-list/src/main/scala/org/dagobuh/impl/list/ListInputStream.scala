/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.impl.list

import org.dagobuh.api.inputstream.InputStream

import scala.language.{higherKinds, implicitConversions}
import scala.reflect.ClassTag

case class ListInputStream[A](context: List[A]) extends InputStream[List, A] {
  import ListInputStream.list2ListInputStream
  override def map[B: ClassTag](func: A => B): InputStream[List, B] = context.map(func)
  override def flatMap[B: ClassTag](func: A => TraversableOnce[B]): InputStream[List, B] = context.flatMap(func)
  override def filter(func: A => Boolean): InputStream[List, A] = context.filter(func)
  override def collect[B: ClassTag](func: PartialFunction[A, B]): InputStream[List, B] = context.collect(func)
  override def inner: List[A] = context
  override def mapInner[B: ClassTag](func: List[A] => List[B]): InputStream[List, B] = func(context)
  override def union(inputStream: InputStream[List, A]): InputStream[List, A] = context ::: inputStream.inner
  override def union(inputStream: TraversableOnce[A]): InputStream[List, A] = inputStream.toList ::: context
}

object ListInputStream {
  implicit private def list2ListInputStream[A](a: List[A]): InputStream[List, A] = ListInputStream(a)
}