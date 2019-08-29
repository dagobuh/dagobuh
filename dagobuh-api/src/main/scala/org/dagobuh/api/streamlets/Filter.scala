/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.streamlets

import org.dagobuh.api.appliers.StreamletApplier
import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds

class Filter[A](func: A => Boolean) {
  def run[F[_]](in: InputStream[F, A]): InputStream[F, A] = {
    in.filter(func)
  }
}

object Filter {
  def apply[A](func: A => Boolean): Filter[A] = new Filter(func)
  implicit def filter[F[_], A]: StreamletApplier[F, Filter[A], A, A] = new StreamletApplier[F, Filter[A], A, A] {
    override def run(in: InputStream[F, A], streamlet: Filter[A]): InputStream[F, A] = streamlet.run(in)
  }
}





