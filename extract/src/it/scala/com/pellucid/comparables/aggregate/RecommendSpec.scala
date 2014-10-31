package com.pellucid.comparables.aggregate

import com.pellucid.comparables.cassandra.CassandraMentionsAggregateServiceModule
import com.pellucid.comparables.schema.{MentionsAggregate, Recommendation, RecommendationRecord}
import com.pellucid.comparables.{SparkCassandraSpec, Ticker}
import com.websudos.phantom.Implicits.{context => _, _}

import scala.concurrent.Await
import scala.concurrent.duration._

class RecommendSpec extends SparkCassandraSpec with CassandraMentionsAggregateServiceModule {

  val MSFT = Ticker("MSFT")
  val FB = Ticker("FB")
  val GOOG = Ticker("GOOG")

  val mentions = Seq(
    MentionsAggregate(FB, FB, 100), MentionsAggregate(FB, GOOG, 80), MentionsAggregate(FB, MSFT, 50)
  )

  override def beforeAll(): Unit = {
    super.beforeAll()
    mentions foreach mentionsAggregateService.insert
  }

  "Recommend" should "compute recommendations" in withSparkContext { sc =>
    
    val recommend = new Recommend(sc, keySpace)
    recommend.recommend()

    val recommendations = Await.result(RecommendationRecord.select.where(_.ticker eqs FB.value).fetch(), 2.seconds).sortBy(_.position)
    assert(recommendations.size == 3)

    // Validate correct recommendations

    assert(recommendations(0) == Recommendation(FB, 0, FB, 1.0))
    assert(recommendations(1) == Recommendation(FB, 1, GOOG, .8))
    assert(recommendations(2) == Recommendation(FB, 2, MSFT, .5))
  }
}
