/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.api.appliers

import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds

trait OutletApplier[F[_], G, H] {
  def run(in: InputStream[F, H], streamlet: G): Unit
}

object OutletApplier {
  def apply[F[_], G, I](implicit streamletApplier: OutletApplier[F, G, I]): OutletApplier[F, G, I] = streamletApplier
}

