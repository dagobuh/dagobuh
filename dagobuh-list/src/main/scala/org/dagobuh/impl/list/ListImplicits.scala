/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package org.dagobuh.impl.list

import org.dagobuh.api.inputstream.{ConvertToInputStream, InputStream}

import scala.language.higherKinds
import scala.reflect.ClassTag

object ListImplicits {
  implicit def convertToInputStream: ConvertToInputStream[List] = new ConvertToInputStream[List] {
    override def convert[A: ClassTag](x: List[A]): InputStream[List, A] = ListInputStream(x)
  }
}
