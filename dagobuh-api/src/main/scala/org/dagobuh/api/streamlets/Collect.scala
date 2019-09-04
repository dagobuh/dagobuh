/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.streamlets

import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds
import scala.reflect.ClassTag

class Collect[F[_], A, B: ClassTag](func: PartialFunction[A, B]) extends Transformer[F, A, B] {
  override def run(input: InputStream[F, A]): InputStream[F, B] = input.collect(func)
}
object Collect {
  def apply[F[_], A, B: ClassTag](func: PartialFunction[A, B]): Collect[F, A, B] = new Collect(func)
}
