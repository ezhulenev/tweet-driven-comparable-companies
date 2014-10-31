package com.pellucid.comparables.service

import com.pellucid.comparables.schema.Mention

import scala.concurrent.Future

trait MentionServiceModule {

  def mentionService: MentionService

  trait MentionService {
    def insert(mention: Mention): Future[Unit]
  }
}
