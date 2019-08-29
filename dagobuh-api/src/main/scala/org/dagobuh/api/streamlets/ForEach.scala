/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.streamlets

import org.dagobuh.api.appliers.StreamletApplier
import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds

class ForEach[A](func: A => Unit) {
  def run[F[_]](in: InputStream[F, A]): InputStream[F, Unit] = {
    in.map(func)
  }
}

object ForEach {
  def apply[A](func: A => Unit): ForEach[A] = new ForEach(func)
  implicit def forEach[F[_], A]: StreamletApplier[F, ForEach[A], A, Unit] = new StreamletApplier[F, ForEach[A], A, Unit] {
    override def run(in: InputStream[F, A], streamlet: ForEach[A]): InputStream[F, Unit] = streamlet.run(in)
  }
}

