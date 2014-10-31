package com.pellucid.comparables

import com.typesafe.config.ConfigFactory

trait TwitterAuthorization {

  private[this] lazy val config = ConfigFactory.load()

  private[this] def setAuthProperty(property: String) = {
    if (!config.hasPath(property))
      sys.error(s"Can't authorize in Twitter. Property is not available in configuration: $property")


    System.setProperty(property, config.getString(property))
  }

  setAuthProperty("twitter4j.oauth.consumerKey")
  setAuthProperty("twitter4j.oauth.consumerSecret")
  setAuthProperty("twitter4j.oauth.accessToken")
  setAuthProperty("twitter4j.oauth.accessTokenSecret")
}
