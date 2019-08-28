/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.inputstream

import scala.language.higherKinds
import scala.reflect.ClassTag

trait ConvertToInputStream[F[_]] {
  def convert[A: ClassTag](x: F[A]): InputStream[F, A]
}
