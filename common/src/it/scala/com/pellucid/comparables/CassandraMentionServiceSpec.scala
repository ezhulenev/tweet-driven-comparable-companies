package com.pellucid.comparables

import com.pellucid.comparables.schema.{Mention, MentionRecord}
import com.pellucid.comparables.cassandra.CassandraMentionServiceModule
import com.websudos.phantom.Implicits.{context => _, _}
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration._

class CassandraMentionServiceSpec extends CassandraServiceSpec with CassandraMentionServiceModule  {

  val mention = Mention(Ticker("AAPL"), "Twitter", "1234567", DateTime.now(), Set("AAPL", "FB", "EBAY") map Ticker)

  "Mentions Service" should "insert new mention" in {

    Await.result(mentionService.insert(mention), 1.second)

    val mentions = Await.result(MentionRecord.select.where(_.ticker eqs "AAPL").fetch(), 2.second)
    assert(mentions.size == 1)
    assert(mentions.head == mention)
  }
}
