package com.pellucid.comparables

import com.pellucid.comparables.cassandra.{ConfigurableCassandraManager, CassandraExecutionContext}
import com.pellucid.comparables.schema.{MentionRecord, MentionsAggregateRecord, RecommendationRecord}
import com.typesafe.config.ConfigFactory
import com.websudos.phantom.testing.CassandraFlatSpec
import com.websudos.phantom.zookeeper.CassandraManager
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.concurrent.Await
import scala.concurrent.duration._

trait SparkCassandraSpec extends CassandraFlatSpec with CassandraExecutionContext {

  def appName: String = "test_extract"

  override def manager: CassandraManager = ConfigurableCassandraManager

  private val config = ConfigFactory.load()

  private val cassandraHost = config.getString("cassandra.host")
  private val nativePort = config.getInt("cassandra.nativePort").toString
  private val rpcPort = config.getInt("cassandra.rpcPort").toString

  lazy val sparkConf = new SparkConf().
    setMaster("local").
    set("spark.cassandra.connection.host", cassandraHost).
    set("spark.cassandra.connection.native.port", nativePort.toString).
    set("spark.cassandra.connection.rpc.port", rpcPort.toString).
  setAppName(appName)

  private lazy val sc = new SparkContext(sparkConf)
  private lazy val ssc = new StreamingContext(sc, Seconds(1))

  private def installSchema() {
    Await.result(MentionRecord.create.future(), 2.seconds)
    Await.result(MentionsAggregateRecord.create.future(), 2.seconds)
    Await.result(RecommendationRecord.create.future(), 2.seconds)
  }

  override def beforeAll() {
    super.beforeAll()

    installSchema()
  }

  def withSparkContext[T](f: SparkContext => T) = {
    try {
      val result = f(sc)
      result
    } catch {
      case err: Throwable =>
        err.printStackTrace()
        sc.stop()
        throw err
    }
  }

  def withStreamingContext[T](f: StreamingContext => T) = {
    try {
      val result = f(ssc)
      ssc.start()
      result
    } catch {
      case err: Throwable =>
        err.printStackTrace()
        ssc.stop()
        throw err
    }
  }

  override def afterAll() {
    super.afterAll()

    ssc.stop()
    sc.stop()
  }
}