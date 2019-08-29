/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.streamlets

import org.dagobuh.api.appliers.StreamletApplier
import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds
import scala.reflect.ClassTag

class Map[A, B: ClassTag](func: A => B) {
  def run[F[_]](in: InputStream[F, A]): InputStream[F, B] = {
    in.map(func)
  }
}


object Map {
  def apply[A, B: ClassTag](func: A => B): Map[A, B] = new Map(func)
  implicit def map[F[_], A, B: ClassTag]: StreamletApplier[F, Map[A, B], A, B] =
    (in: InputStream[F, A], streamlet: Map[A, B]) => streamlet.run(in)
}


