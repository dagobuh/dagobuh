/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.appliers

import scala.language.higherKinds
import scala.reflect.ClassTag

trait StatefulFunctionApplier[F[_], A[_, _]] {
  def applyFunc[T, U: ClassTag](context: F[T], func: A[T, U]): F[U]
}
