/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.impl.flink

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.dagobuh.api.appliers.StatefulFunctionApplier
import org.dagobuh.api.inputstream.InputStream

import scala.language.{higherKinds, implicitConversions}
import scala.reflect.ClassTag

class FlinkInputStream[A: ClassTag](context: DataStream[A]) extends InputStream[DataStream, A] {
  import FlinkInputStream.dataStream2FlinkInputStream
  override def map[B: ClassTag](func: A => B): InputStream[DataStream, B] = context.map(func)(TypeInformation.of(implicitly[ClassTag[B]].runtimeClass.asInstanceOf[Class[B]]))
  override def flatMap[B: ClassTag](func: A => TraversableOnce[B]): InputStream[DataStream, B] = context.flatMap(func)(TypeInformation.of(implicitly[ClassTag[B]].runtimeClass.asInstanceOf[Class[B]]))
  override def filter(func: A => Boolean): InputStream[DataStream, A] = context.filter(func)
  override def collect[B: ClassTag](func: PartialFunction[A, B]): InputStream[DataStream, B] = context.flatMap(func.lift(_))(TypeInformation.of(implicitly[ClassTag[B]].runtimeClass.asInstanceOf[Class[B]]))
  override def inner: DataStream[A] = context
  override def mapInner[B: ClassTag](func: DataStream[A] => DataStream[B]): InputStream[DataStream, B] = func(context)
  override def union(inputStream: InputStream[DataStream, A]): InputStream[DataStream, A] = context.union(inputStream.inner)
  override def union(inputStream: TraversableOnce[A]): InputStream[DataStream, A] = union(context.executionEnvironment.fromCollection(inputStream.toIterator)(TypeInformation.of(implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[A]])))
}

object FlinkInputStream {
  implicit private def dataStream2FlinkInputStream[A: ClassTag](a: DataStream[A]): InputStream[DataStream, A] = FlinkInputStream(a)

  def apply[A: ClassTag](context: DataStream[A]): InputStream[DataStream, A] = new FlinkInputStream[A](context)
}