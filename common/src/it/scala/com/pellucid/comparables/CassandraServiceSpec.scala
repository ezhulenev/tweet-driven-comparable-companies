package com.pellucid.comparables

import com.pellucid.comparables.cassandra.ConfigurableCassandraManager
import com.pellucid.comparables.schema.{MentionRecord, MentionsAggregateRecord, RecommendationRecord}
import com.websudos.phantom.testing.CassandraFlatSpec
import com.websudos.phantom.zookeeper.CassandraManager

import scala.concurrent.Await
import scala.concurrent.duration._

trait CassandraServiceSpec extends CassandraFlatSpec {

  override def manager: CassandraManager = ConfigurableCassandraManager

  override def beforeAll() {
    super.beforeAll()

    Await.result(MentionRecord.create.future(), 2.seconds)
    Await.result(MentionsAggregateRecord.create.future(), 2.seconds)
    Await.result(RecommendationRecord.create.future(), 2.seconds)
  }
}
