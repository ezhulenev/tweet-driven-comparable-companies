package com.pellucid.comparables

import com.pellucid.comparables.schema.{MentionsAggregate, MentionsAggregateRecord}
import com.pellucid.comparables.cassandra.CassandraMentionsAggregateServiceModule
import com.websudos.phantom.Implicits.{context => _, _}

import scala.concurrent.Await
import scala.concurrent.duration._

class CassandraMentionsAggregateServiceSpec extends CassandraServiceSpec with CassandraMentionsAggregateServiceModule  {

  val mention = MentionsAggregate(Ticker("AAPL"), Ticker("FB"), 100)

  "MentionsAggregates Service" should "insert new mentions aggregate" in {

    Await.result(mentionsAggregateService.insert(mention), 1.second)

    val mentions = Await.result(MentionsAggregateRecord.select.where(_.ticker eqs "AAPL").fetch(), 2.second)
    assert(mentions.size == 1)
    assert(mentions.head == mention)
  }
}
