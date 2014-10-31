package com.pellucid.comparables

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import spray.can.Http

object SuggestionServer extends App {
  val log = LoggerFactory.getLogger(this.getClass)

  val config = ConfigFactory.load()

  implicit val system = ActorSystem("suggestion")

  val interface = config.getString("server.interface")
  val port = config.getInt("server.port")

  val service = system.actorOf(Props[SuggestionService], "suggestion-service")

  log.info(s"Start Suggestion Service on interface: $interface. Port: $port.")

  IO(Http) ! Http.Bind(service, interface, port)
}