/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.appliers

import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds
import scala.reflect.ClassTag

trait StatefulFunctionApplier[F[_], A[_, _]] {
  def apply[T, U: ClassTag](context: InputStream[F, T], func: A[T, U]): InputStream[F, U]
}

object StatefulFunctionApplier {
  def apply[F[_], A[_, _]](implicit statefulFunctionApplier: StatefulFunctionApplier[F, A]): StatefulFunctionApplier[F, A] = statefulFunctionApplier
}
