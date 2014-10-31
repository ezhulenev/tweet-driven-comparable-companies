package com.pellucid.comparables.aggregate

import com.datastax.spark.connector._
import com.pellucid.comparables.CassandraMappers
import com.pellucid.comparables.schema._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.slf4j.LoggerFactory

class Recommend(@transient sc: SparkContext, keyspace: String) extends CassandraMappers with Serializable {
  private val log = LoggerFactory.getLogger(classOf[Recommend])

  private def toRecommendation: (MentionsAggregate, Int) => Recommendation = {
    var totalMentions: Option[Long] = None

    {
      case (aggregate, idx) if totalMentions.isEmpty =>
        totalMentions = Some(aggregate.count)
        Recommendation(aggregate.ticker, idx, aggregate.mentionedWith, 1)

      case (aggregate, idx) =>
        Recommendation(aggregate.ticker, idx, aggregate.mentionedWith, aggregate.count.toDouble / totalMentions.get)
    }
  }

  def recommend(): Unit = {
    log.info(s"Calculate recommendations")

    val aggregates = sc.cassandraTable[MentionsAggregate](keyspace, MentionsAggregateRecord.tableName).sortBy(_.count, ascending = false)

    val recommendations = aggregates.
      groupBy(_.ticker).
      mapValues(_.zipWithIndex).
      flatMapValues(_ map toRecommendation.tupled).values

    recommendations.saveToCassandra(keyspace, RecommendationRecord.tableName)
  }
}