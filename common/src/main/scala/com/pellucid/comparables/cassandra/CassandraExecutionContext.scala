package com.pellucid.comparables.cassandra

import java.util.concurrent.Executors

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

object CassandraExecutionContext {
  implicit val executionContext: ExecutionContext = {
    val executor = {
      val threadFactory = new ThreadFactoryBuilder().setNameFormat("cassandra-pool-%d").build()
      val cassandraConcurrency = ConfigFactory.load().getInt("cassandra.concurrency")
      Executors.newFixedThreadPool(cassandraConcurrency, threadFactory)
    }
    ExecutionContext.fromExecutor(executor)
  }
}

trait CassandraExecutionContext {
  implicit val cassandraExecutionContext: ExecutionContext =
    CassandraExecutionContext.executionContext
}
