package com.pellucid.comparables.ingest

import com.datastax.spark.connector.streaming._
import com.pellucid.comparables.CassandraMappers
import com.pellucid.comparables.schema.{Mention, MentionRecord}
import org.apache.spark.streaming.dstream.DStream

class MentionStreamFunctions(@transient stream: DStream[Mention]) extends CassandraMappers with Serializable {

  def saveMentionsToCassandra(keyspace: String) = {
    stream.saveToCassandra(keyspace, MentionRecord.tableName)
  }
}
