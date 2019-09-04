package org.dagobuh.api.streamlets
import org.dagobuh.api.inputstream.InputStream

import scala.language.higherKinds

case class CompositeTransformer[F[_], A, B, C](f: Transformer[F, A, B], g: Transformer[F, C, A]) extends Transformer[F, C, B] {
  override def run(input: InputStream[F, C]): InputStream[F, B] = f.run(g.run(input))
}
