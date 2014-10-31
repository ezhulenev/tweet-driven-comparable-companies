package com.pellucid.comparables

import com.pellucid.comparables.schema.{Recommendation, RecommendationRecord}
import com.pellucid.comparables.cassandra.CassandraRecommendationServiceModule
import com.websudos.phantom.Implicits.{context => _, _}

import scala.concurrent.Await
import scala.concurrent.duration._

class CassandraRecommendationServiceSpec extends CassandraServiceSpec with CassandraRecommendationServiceModule  {

  val recommendation = Recommendation(Ticker("AAPL"), 3, Ticker("FB"), 0.8)

  "Recommendations Service" should "insert new recommendation" in {

    Await.result(recommendationService.insert(recommendation), 1.second)

    val mentions = Await.result(RecommendationRecord.select.where(_.ticker eqs "AAPL").fetch(), 2.second)
    assert(mentions.size == 1)
    assert(mentions.head == recommendation)
  }
}
