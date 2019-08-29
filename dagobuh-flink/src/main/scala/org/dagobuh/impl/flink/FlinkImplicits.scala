/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.impl.flink

import org.apache.flink.api.common.functions.{FlatMapFunction, MapFunction}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.dagobuh.api.appliers.StatefulFunctionApplier
import org.dagobuh.api.inputstream.{ConvertToInputStream, InputStream}

import scala.language.{higherKinds, implicitConversions}
import scala.reflect.ClassTag

object FlinkImplicits {
  type StatefulFlatmapFunctionApplier[F[_]] = StatefulFunctionApplier[F, FlatMapFunction]
  type StatefulMapFunctionApplier[F[_]] = StatefulFunctionApplier[F, MapFunction]

  implicit val statefulFlinkFlatmapFunction: StatefulFlatmapFunctionApplier[DataStream] =
    new StatefulFunctionApplier[DataStream, FlatMapFunction] {
      override def apply[A, B: ClassTag](inputStream: InputStream[DataStream, A], func: FlatMapFunction[A, B]): InputStream[DataStream, B] = inputStream.mapInner(_.flatMap(func)(TypeInformation.of(implicitly[ClassTag[B]].runtimeClass.asInstanceOf[Class[B]])))
    }

  implicit val statefulFlinkMapFunction: StatefulMapFunctionApplier[DataStream] =
    new StatefulFunctionApplier[DataStream, MapFunction] {
      override def apply[A, B: ClassTag](inputStream: InputStream[DataStream, A], func: MapFunction[A, B]): InputStream[DataStream, B] = inputStream.mapInner(_.map(func)(TypeInformation.of(implicitly[ClassTag[B]].runtimeClass.asInstanceOf[Class[B]])))
    }

  implicit def convertToInputStream: ConvertToInputStream[DataStream] = new ConvertToInputStream[DataStream] {
    override def convert[A: ClassTag](x: DataStream[A]): InputStream[DataStream, A] = FlinkInputStream(x)
  }
}
