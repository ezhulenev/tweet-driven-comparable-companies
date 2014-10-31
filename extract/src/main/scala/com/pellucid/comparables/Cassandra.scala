package com.pellucid.comparables

import com.pellucid.comparables.cassandra.CassandraService
import com.pellucid.comparables.schema.{RecommendationRecord, MentionsAggregateRecord, MentionRecord}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

trait Cassandra extends CassandraService {

  private[this] val config = ConfigFactory.load()

  protected val cassandraHost = config.getString("cassandra.host")
  protected val nativePort = config.getInt("cassandra.nativePort").toString
  protected val rpcPort = config.getInt("cassandra.rpcPort").toString

  def installSchema() {
    Await.result(MentionRecord.create.future(), 30.seconds)
    Await.result(MentionsAggregateRecord.create.future(), 30.seconds)
    Await.result(RecommendationRecord.create.future(), 30.seconds)
  }

}
