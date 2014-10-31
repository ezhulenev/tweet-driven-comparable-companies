package com.pellucid.comparables.schema

import com.datastax.driver.core.Row
import com.pellucid.comparables.Ticker
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey

/**
 * Count mentions for each ticker pair
 *
 * @param ticker        ticker of focus company
 * @param mentionedWith mentioned with this ticker
 * @param count         number of mentions
 */
case class MentionsAggregate(ticker: Ticker, mentionedWith: Ticker, count: Long)

sealed class MentionsAggregateRecord extends CassandraTable[MentionsAggregateRecord, MentionsAggregate] {

  override val tableName: String = "mentions_aggregate"

  object ticker         extends StringColumn (this) with PartitionKey[String]
  object mentioned_with extends StringColumn (this) with PrimaryKey[String]
  object counter        extends LongColumn   (this)

  def fromRow(r: Row): MentionsAggregate = {
    MentionsAggregate(Ticker(ticker(r)), Ticker(mentioned_with(r)), counter(r))
  }
}

object MentionsAggregateRecord extends MentionsAggregateRecord