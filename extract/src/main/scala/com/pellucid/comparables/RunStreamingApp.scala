package com.pellucid.comparables

import java.util.concurrent.{Executors, TimeUnit}

import com.pellucid.comparables.aggregate.{AggregateMentions, Recommend}
import com.pellucid.comparables.ingest._
import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

import scala.language.implicitConversions

class RunStreamingApp extends TwitterAuthorization with Cassandra

object RunStreamingApp extends RunStreamingApp with App {
  private val log = LoggerFactory.getLogger(classOf[RunStreamingApp])

  private val config = ConfigFactory.load()

  private val filters = Companies.load().map(c => s"$$${c.ticker.value}").take(config.getInt("filters"))

  private val scheduler = Executors.newScheduledThreadPool(1)

  // Install Schema into Cassandra
  installSchema()

  // Prepare Spark Config
  val sparkConf = new SparkConf().
    setMaster(config.getString("spark.master")).
    setJars(SparkContext.jarOfClass(this.getClass).toSeq).
    set("spark.cassandra.connection.host", cassandraHost).
    set("spark.cassandra.connection.native.port", nativePort.toString).
    set("spark.cassandra.connection.rpc.port", rpcPort.toString).
    setAppName("TwitterComparableCompanies")

  val sc = new SparkContext(sparkConf)
  val ssc = new StreamingContext(sc, Seconds(2))

  val stream = TwitterUtils.createStream(ssc, None, filters = filters)
  val aggregate = new AggregateMentions(sc, keySpace)
  val recommend = new Recommend(sc, keySpace)

  // Save Twitter Stream to cassandra
  stream.foreachRDD(updates => log.info(s"Received Twitter stream updates. Count: ${updates.count()}"))
  stream.extractMentions.saveMentionsToCassandra(keySpace)

  // Schedule Aggregation & Recommendation
  implicit def toRunnable[F](f: => F) = new Runnable() { def run() { f } }

  def aggregateAndRecommend() = {
    log.info("Run scheduled aggregation and recommendation")
    aggregate.aggregate()
    recommend.recommend()
  }
  scheduler.scheduleWithFixedDelay(aggregateAndRecommend(), 10, 10, TimeUnit.MINUTES)

  // Start Streaming App
  log.info("Start Spark Streaming Context")
  ssc.start()
}