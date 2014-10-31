package com.pellucid.comparables.schema

import com.datastax.driver.core.Row
import com.pellucid.comparables.Ticker
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey

/**
 * Recommendation built based on company mentions with other companies
 *
 * @param ticker         focus company ticker
 * @param position       recommendation position
 * @param recommendation recommended company ticker
 * @param p              number of times recommended company mentioned together
 *                       with focus company divided by total focus company mentions
 */
case class Recommendation(ticker: Ticker, position: Long, recommendation: Ticker, p: Double)

sealed class RecommendationRecord extends CassandraTable[RecommendationRecord, Recommendation] {

  override val tableName: String = "recommendation"

  object ticker         extends StringColumn (this) with PartitionKey[String]
  object position       extends LongColumn   (this) with PrimaryKey[Long]
  object recommendation extends StringColumn (this)
  object p              extends DoubleColumn (this)

  def fromRow(r: Row): Recommendation = {
    Recommendation(Ticker(ticker(r)), position(r), Ticker(recommendation(r)), p(r))
  }
}

object RecommendationRecord extends RecommendationRecord