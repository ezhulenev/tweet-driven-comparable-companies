package com.pellucid.comparables.cassandra

import com.pellucid.comparables.schema.{Recommendation, RecommendationRecord}
import com.pellucid.comparables.service.RecommendationServiceModule
import org.slf4j.LoggerFactory

trait CassandraRecommendationServiceModule extends RecommendationServiceModule with CassandraService {

  object recommendationService extends RecommendationService {
    private val log = LoggerFactory.getLogger(classOf[CassandraRecommendationServiceModule])

    def insert(recommendation: Recommendation) = {
      log.trace(
        s"Insert recommendation. Ticker: ${recommendation.ticker.value}. " +
          s"Position: ${recommendation.position}. " +
          s"Recommended ticker: ${recommendation.ticker}. " +
          s"P: ${recommendation.p}"
      )

      RecommendationRecord.insert.
        value(_.ticker, recommendation.ticker.value).
        value(_.position, recommendation.position).
        value(_.recommendation, recommendation.recommendation.value).
        value(_.p, recommendation.p).
        future().
        map(_ => ())
    }
  }

}
