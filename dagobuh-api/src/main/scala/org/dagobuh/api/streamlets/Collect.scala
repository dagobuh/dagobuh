/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.streamlets

import org.dagobuh.api.appliers.StreamletApplier
import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds
import scala.reflect.ClassTag

class Collect[A, B: ClassTag](func: PartialFunction[A, B]) {
  def run[F[_]](in: InputStream[F, A]): InputStream[F, B] = {
    in.collect(func)
  }
}

object Collect {
  def apply[A, B: ClassTag](func: PartialFunction[A, B]): Collect[A, B] = new Collect(func)
  implicit def collect[F[_], A, B: ClassTag]: StreamletApplier[F, Collect[A, B], A, B] = new StreamletApplier[F, Collect[A, B], A, B] {
    override def run(in: InputStream[F, A], streamlet: Collect[A, B]): InputStream[F, B] = streamlet.run(in)
  }
}






