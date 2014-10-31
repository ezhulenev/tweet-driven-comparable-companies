package com.pellucid.comparables.service

import com.pellucid.comparables.schema.MentionsAggregate

import scala.concurrent.Future

trait MentionsAggregateServiceModule {

  def mentionsAggregateService: MentionsAggregateService

  trait MentionsAggregateService {
    def insert(MentionsAggregate: MentionsAggregate): Future[Unit]
  }
}
