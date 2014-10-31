package com.pellucid.comparables.cassandra

import com.pellucid.comparables.schema.{MentionsAggregate, MentionsAggregateRecord}
import com.pellucid.comparables.service.MentionsAggregateServiceModule
import org.slf4j.LoggerFactory

trait CassandraMentionsAggregateServiceModule extends MentionsAggregateServiceModule with CassandraService {

  object mentionsAggregateService extends MentionsAggregateService {
    private val log = LoggerFactory.getLogger(classOf[CassandraMentionsAggregateServiceModule])

    def insert(mentionsAggregate: MentionsAggregate) = {
      log.trace(
        s"Insert mentions aggregate. Ticker: ${mentionsAggregate.ticker.value}. " +
          s"Mentioned with: ${mentionsAggregate.mentionedWith.value}. " +
          s"Count: ${mentionsAggregate.count}"
      )

      MentionsAggregateRecord.insert.
        value(_.ticker, mentionsAggregate.ticker.value).
        value(_.mentioned_with, mentionsAggregate.mentionedWith.value).
        value(_.counter, mentionsAggregate.count).
        future().
        map(_ => ())
    }
  }

}
