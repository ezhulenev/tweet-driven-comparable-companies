package com.pellucid.comparables.ingest

import org.apache.spark.streaming.dstream.DStream

class SourceStreamFunctions[T: MentionsExtractor](@transient stream: DStream[T]) extends Serializable {
  def extractMentions =
    stream.flatMap(s => implicitly[MentionsExtractor[T]].mentions(s))
}