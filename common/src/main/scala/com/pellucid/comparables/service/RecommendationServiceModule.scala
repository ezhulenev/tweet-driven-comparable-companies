package com.pellucid.comparables.service

import com.pellucid.comparables.schema.Recommendation

import scala.concurrent.Future

trait RecommendationServiceModule {

  def recommendationService: RecommendationService

  trait RecommendationService {
    def insert(mention: Recommendation): Future[Unit]
  }
}
