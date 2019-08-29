/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.appliers


import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds

trait StreamletApplier[F[_], G, H, I] {
  def run(in: InputStream[F, H], streamlet: G): InputStream[F, I]
}

object StreamletApplier {
  def apply[F[_], G, H, I](implicit streamletApplier: StreamletApplier[F, G, H, I]): StreamletApplier[F, G, H, I] = streamletApplier
}
