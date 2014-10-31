package com.pellucid.comparables.cassandra

import com.datastax.driver.core.{Cluster, Session}
import com.typesafe.config.ConfigFactory
import com.websudos.phantom.zookeeper.{CassandraManager, SimpleCassandraConnector}

import scala.concurrent._

private[cassandra] case object CassandraInitLock

trait CassandraService extends SimpleCassandraConnector with CassandraExecutionContext {
  def keySpace: String = {
    val config = ConfigFactory.load()
    config.getString("cassandra.keyspace")
  }

  override def manager: CassandraManager = ConfigurableCassandraManager
}

object ConfigurableCassandraManager extends CassandraManager {

  private[this] val config = ConfigFactory.load()

  val cassandraHost = config.getString("cassandra.host")
  val nativePort = config.getInt("cassandra.nativePort")

  val livePort = 9042
  val embeddedPort = nativePort

  private[this] var initialized = false
  @volatile private[this] var _session: Session = null

  lazy val cluster: Cluster = Cluster.builder()
    .addContactPoint(cassandraHost)
    .withPort(nativePort)
    .withoutJMXReporting()
    .withoutMetrics()
    .build()

  def session = _session

  def initIfNotInited(keySpace: String): Unit = CassandraInitLock.synchronized {
    val strategy = config.getString("cassandra.replication.strategy")
    val factor = config.getInt("cassandra.replication.factor")
    
    if (!initialized) {
      _session = blocking {
        val s = cluster.connect()
        s.execute(s"CREATE KEYSPACE IF NOT EXISTS $keySpace WITH replication = {'class': '$strategy', 'replication_factor' : $factor};")
        s.execute(s"USE $keySpace;")
        s
      }
      initialized = true
    }
  }
}