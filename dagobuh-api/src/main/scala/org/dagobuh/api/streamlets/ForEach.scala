/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.streamlets

import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds

class ForEach[F[_], A](func: A => Unit) extends Sink[F, A] {
  def run(in: InputStream[F, A]): Unit = {
    in.map(func)
  }
}

object ForEach {
  def apply[F[_], A](func: A => Unit): ForEach[F, A] = new ForEach(func)
}