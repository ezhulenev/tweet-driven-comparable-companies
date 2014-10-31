package com.pellucid.comparables

import com.datastax.spark.connector.mapper.DefaultColumnMapper
import com.datastax.spark.connector.types.TypeConverter
import com.pellucid.comparables.schema.{Recommendation, MentionsAggregate, Mention}

import scala.reflect.runtime.universe._

private[this] object TickerTypeConverter {

  object StringToTickerTypeConverter extends TypeConverter[Ticker] {
    def targetTypeTag = typeTag[Ticker]
    def convertPF = { case str: String => Ticker(str) }
  }

  object TickerToStringTypeConverter extends TypeConverter[String] {
    def targetTypeTag = typeTag[String]
    def convertPF = { case Ticker(str) => str }
  }

}

/**
 * Custom fields mapping from Phantom Records to Spark Cassandra connection
 */
trait CassandraMappers {

  import TickerTypeConverter._

  TypeConverter.registerConverter(StringToTickerTypeConverter)
  TypeConverter.registerConverter(TickerToStringTypeConverter)

  implicit object MentionMapper extends DefaultColumnMapper[Mention](Map(
    "ticker"        -> "ticker",
    "source"        -> "source",
    "sourceId"      -> "source_id",
    "time"          -> "time",
    "mentions"      -> "mentions"
  ))

  implicit object MentionAggregateMapper extends DefaultColumnMapper[MentionsAggregate](Map(
    "ticker"         -> "ticker",
    "mentionedWith"  -> "mentioned_with",
    "count"          -> "counter"
  ))

  implicit object RecommendationMapper extends DefaultColumnMapper[Recommendation](Map(
    "ticker"         -> "ticker",
    "position"       -> "position",
    "recommendation" -> "recommendation",
    "p"              -> "p"
  ))

}
