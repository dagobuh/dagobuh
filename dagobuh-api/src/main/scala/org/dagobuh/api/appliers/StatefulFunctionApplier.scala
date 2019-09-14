/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.appliers

import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds
import scala.reflect.ClassTag

/**
 * Type class for applying stateful functions to underlying
 * stream backend. For example a flink FlatMapFunction could be applied to a DataStream
 * by creating an implicit instance of type StatefulFunctionApplier[DataStream, FlatMapFunction].
 *
 * Used in a Streamlet by applying a context bound to InputStream's F[_].
 *
 * ==Flink Example==
 * {{{
    type StatefulFlatMapFunctionApplier[F[_]] = StatefulFunctionApplier[F, FlatMapFunction]

    implicit val statefulFlinkFlatmapFunction: StatefulFlatMapFunctionApplier[DataStream] =
      new StatefulFunctionApplier[DataStream, FlatMapFunction] {
        override def apply[A, B: ClassTag](func: FlatMapFunction[A, B])(inputStream: InputStream[DataStream, A]): InputStream[DataStream, B] = inputStream.mapInner(_.flatMap(func)(TypeInformation.of(implicitly[ClassTag[B]].runtimeClass.asInstanceOf[Class[B]])))
      }

    class FooStreamlet[F[_]: StatefulFlatMapFunctionApplier] extends Transformer[F, String, String] {
      override def run(input: InputStream[F, String]): InputStream[F, String] = {
        val filterFunc: FlatMapFunction[String, String] = (value: String, out: Collector[String]) => {
          if (value.length > 10) {
            out.collect(value)
          }
        }
        implicitly[StatefulFlatMapFunctionApplier[F]].apply(filterFunc)(input)
      }
    }
 * }}}
 *
 * In the above example, FooStreamlet knows nothing about the underlying streaming backend, it only needs to know
 * that it supports stateful functions. The underlying code is defined in backends, like the Flink backend, and will allow
 * Streamlets to be as coupled to the backend as is necessary (or convenient). Given a BEAM backend, the same streamlet
 * could be reused assuming a StatefulFunctionApplier instance is defined for BEAM's PCollection.
 *
 * @tparam F underlying streaming type
 * @tparam A higher kinded function type A[Input, Output]
 */
trait StatefulFunctionApplier[F[_], A[_, _]] {
  def apply[T, U: ClassTag](func: A[T, U])(context: InputStream[F, T]): InputStream[F, U]
}

object StatefulFunctionApplier {
  def apply[F[_], A[_, _]](implicit statefulFunctionApplier: StatefulFunctionApplier[F, A]): StatefulFunctionApplier[F, A] = statefulFunctionApplier
}
