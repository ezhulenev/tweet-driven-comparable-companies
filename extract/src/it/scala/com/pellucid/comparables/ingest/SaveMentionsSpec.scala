package com.pellucid.comparables.ingest

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.pellucid.comparables.{SparkCassandraSpec, Ticker}
import com.pellucid.comparables.cassandra.CassandraMentionServiceModule
import com.pellucid.comparables.schema.{Mention, MentionRecord}
import com.websudos.phantom.Implicits.{context => _, _}
import org.apache.spark.rdd.RDD
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

class SaveMentionsSpec extends SparkCassandraSpec with CassandraMentionServiceModule {

  private val log = LoggerFactory.getLogger(classOf[SaveMentionsSpec])

  val AAPL = Ticker("AAPL")
  val FB = Ticker("FB")
  val GOOG = Ticker("GOOG")

  def mention(focus: Ticker, mentions: Ticker*) = {
    Mention(focus, "Twitter", UUID.randomUUID().toString, DateTime.now(), (focus +: mentions).toSet)
  }

  val mentions = Seq(
    mention(AAPL, FB, GOOG), mention(AAPL, GOOG), mention(AAPL,FB),mention(AAPL, GOOG, FB),
    mention(GOOG, AAPL)
  )

  "Mentions Stream" should "be persisted in Cassandra" in {
    withStreamingContext { ssc =>

      val rdd = ssc.sparkContext.makeRDD[Mention](mentions)
      val queue: mutable.Queue[RDD[Mention]] = mutable.Queue(rdd)
      val stream = ssc.queueStream(queue, oneAtATime = true)

      stream.saveMentionsToCassandra(keySpace)
    }
    
    // Let stream to be saved in Cassandra
    Thread.sleep(TimeUnit.SECONDS.toMillis(2))

    val fetched = Await.result(MentionRecord.select.where(_.ticker eqs AAPL.value).fetch(), 2.seconds)

    log.info(s"Fetched mentions size: ${fetched.size}")
    fetched.foreach(m => log.info(s" - $m"))

    assert(fetched.size == 4)
  }
}
