package com.pellucid.comparables.aggregate

import java.util.UUID

import com.websudos.phantom.Implicits.{context => _, _}

import com.pellucid.comparables.schema.{MentionsAggregateRecord, Mention}
import com.pellucid.comparables.{Ticker, SparkCassandraSpec}
import com.pellucid.comparables.cassandra.CassandraMentionServiceModule
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration._

class AggregateMentionsSpec extends SparkCassandraSpec with CassandraMentionServiceModule {

  val MSFT = Ticker("MSFT")
  val FB = Ticker("FB")
  val GOOG = Ticker("GOOG")

  def mention(focus: Ticker, mentions: Ticker*) = {
    Mention(focus, "Twitter", UUID.randomUUID().toString, DateTime.now(), (focus +: mentions).toSet)
  }

  val mentions = Seq(
    mention(MSFT, FB, GOOG), mention(MSFT, GOOG), mention(MSFT, FB), mention(MSFT, GOOG, FB), mention(MSFT, FB)
  )

  override def beforeAll(): Unit = {
    super.beforeAll()
    mentions foreach mentionService.insert
  }

  "Aggregate Mentions" should "correctly build aggregates" in withSparkContext { sc =>
    val mentionAggregation = new AggregateMentions(sc, keySpace)
    mentionAggregation.aggregate()

    val aggregates = Await.result(MentionsAggregateRecord.select.where(_.ticker eqs MSFT.value).fetch(), 2.seconds)
    assert(aggregates.size == 3)

    // Validate correct mentions

    val google = aggregates.find(_.mentionedWith == GOOG).get
    assert(google.count == 3)

    val fb = aggregates.find(_.mentionedWith == FB).get
    assert(fb.count == 4)
  }
}
