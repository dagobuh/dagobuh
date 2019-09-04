/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.streamlets

import org.dagobuh.api.inputstream.{ConvertToInputStream, InputStream}

import scala.language.higherKinds
import scala.reflect.ClassTag

/**
  * Used to generate InputStreams in a dag.
  *
  * Is useful for mocking out other possible inputs like Kafka for testing
  *
  * @param func Data generation function
  * @tparam F the collection
  * @tparam A the elements
  */
class DataGenerator[F[_]: ConvertToInputStream, A: ClassTag](func: => F[A]) extends Source[F, A] {
  def run(): InputStream[F, A] = {
    // Map after conversion converts Foo object to GenericType<Foo>
    // this allows fan-in union operations to work oddly...
    implicitly[ConvertToInputStream[F]].convert(func).map(identity)
  }
}

object DataGenerator {
  def apply[F[_]: ConvertToInputStream, A: ClassTag](func: => F[A]): DataGenerator[F, A] = new DataGenerator(func)
}
