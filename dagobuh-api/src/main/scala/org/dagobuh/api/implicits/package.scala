package org.dagobuh.api

import org.dagobuh.api.streamlets.{CompositeTransformer, Transformer}

import scala.language.{higherKinds, implicitConversions}

package object implicits {
  implicit def transformer2transformerOps[F[_], A, B](transformer: Transformer[F, A, B]): TransformerOps[F, A, B] = new TransformerOps(transformer)

  class TransformerOps[F[_], A, B](val transformer: Transformer[F, A, B]) extends AnyVal {
    def compose[C](other: Transformer[F, C, A]): CompositeTransformer[F, A, B, C] = {
      CompositeTransformer(transformer, other)
    }
  }
}
