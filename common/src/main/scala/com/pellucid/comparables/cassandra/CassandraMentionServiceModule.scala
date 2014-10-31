package com.pellucid.comparables.cassandra

import com.pellucid.comparables.schema.{Mention, MentionRecord}
import com.pellucid.comparables.service.MentionServiceModule
import org.slf4j.LoggerFactory

trait CassandraMentionServiceModule extends MentionServiceModule with CassandraService {

  object mentionService extends MentionService {
    private val log = LoggerFactory.getLogger(classOf[CassandraMentionServiceModule])

    def insert(mention: Mention) = {
      log.trace(
        s"Insert mention. Ticker: ${mention.ticker.value}. " +
        s"Source: ${mention.sourceId} @ ${mention.source}. " +
        s"Time: ${mention.time}. " +
        s"Mentioned with: [${mention.mentions.map(_.value).mkString(", ")}]"
      )

      MentionRecord.insert.
        value(_.ticker, mention.ticker.value).
        value(_.source, mention.source).
        value(_.time, mention.time).
        value(_.source_id, mention.sourceId).
        value(_.mentions, mention.mentions.map(_.value)).
        future().
        map(_ => ())
    }
  }

}
