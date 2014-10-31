package com.pellucid.comparables.aggregate

import com.datastax.spark.connector._
import com.pellucid.comparables.schema.{Mention, MentionRecord, MentionsAggregate, MentionsAggregateRecord}
import com.pellucid.comparables.{CassandraMappers, Ticker}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.slf4j.LoggerFactory

import scalaz.std.map._
import scalaz.{Monoid, Semigroup}

class AggregateMentions(@transient sc: SparkContext, keyspace: String) extends CassandraMappers with Serializable {
  private val log = LoggerFactory.getLogger(classOf[AggregateMentions])

  private type Counter = Map[Ticker, Long]

  private implicit lazy val summ = Semigroup.instance[Long](_ + _)

  private lazy val seqOp: (Counter, Ticker) => Counter = {
    case (counter, ticker) if counter.isDefinedAt(ticker) => counter.updated(ticker, counter(ticker) + 1)
    case (counter, ticker) => counter + (ticker -> 1)
  }

  private lazy val combOp: (Counter, Counter) => Counter = {
    case (l, r) => implicitly[Monoid[Counter]].append(l, r)
  }

  def aggregate(): Unit = {
    log.info(s"Calculate mentions aggregates")

    // Emit pairs of (Focus Company Ticker, Mentioned With)
    val pairs = sc.cassandraTable[Mention](keyspace, MentionRecord.tableName).
      flatMap(mention => mention.mentions.map((mention.ticker, _)))

    // Calculate mentions for each ticker
    val aggregated = pairs.aggregateByKey(Map.empty[Ticker, Long])(seqOp, combOp)

    // Build MentionsAggregate from counters
    val mentionsAggregate = aggregated flatMap {
      case (ticker, counter) => counter map {
        case (mentionedWith, count) => MentionsAggregate(ticker, mentionedWith, count)
      }
    }

    mentionsAggregate.saveToCassandra(keyspace, MentionsAggregateRecord.tableName)
  }
}