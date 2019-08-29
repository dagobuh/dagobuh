/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.appliers

import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds

trait InletApplier[F[_], G, I] {
  def run(streamlet: G): InputStream[F, I]
}

object InletApplier {
  def apply[F[_], G, I](implicit streamletApplier: InletApplier[F, G, I]): InletApplier[F, G, I] = streamletApplier
}
