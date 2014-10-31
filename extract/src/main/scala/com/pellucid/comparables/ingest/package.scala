package com.pellucid.comparables

import com.pellucid.comparables.schema.Mention
import org.apache.spark.streaming.dstream.DStream
import scala.language.implicitConversions

package object ingest {
  implicit def toMentionFunctions(ds: DStream[Mention]): MentionStreamFunctions =
    new MentionStreamFunctions(ds)

  implicit def toSourceStreamFunctions[T: MentionsExtractor](ds: DStream[T]): SourceStreamFunctions[T] =
    new SourceStreamFunctions(ds)
}
