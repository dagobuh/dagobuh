package org.dagobuh.api

import org.dagobuh.api.inputstream.InputStream
import org.dagobuh.api.streamlets.{CompositeTransformer, Source, Transformer}

import scala.language.{higherKinds, implicitConversions}

package object implicits {
  implicit def transformer2transformerOps[F[_], A, B](transformer: Transformer[F, A, B]): TransformerOps[F, A, B] = new TransformerOps(transformer)
  implicit def source2sourceOps[F[_], A, B](source: Source[F, A]): SourceOps[F, A] = new SourceOps(source)

  class TransformerOps[F[_], A, B](val transformer: Transformer[F, A, B]) extends AnyVal {
    def compose[C](other: Transformer[F, C, A]): CompositeTransformer[F, A, B, C] = {
      CompositeTransformer(transformer, other)
    }
  }

  class SourceOps[F[_], A](val source: Source[F, A]) extends AnyVal {
    def union(other: Source[F, A]): Source[F, A] = new Source[F, A] {
      override def run(): InputStream[F, A] = source.run().union(other.run())
    }
  }
}
