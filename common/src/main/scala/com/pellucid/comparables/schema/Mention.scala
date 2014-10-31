package com.pellucid.comparables.schema

import com.datastax.driver.core.Row
import com.pellucid.comparables.Ticker
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.Implicits._
import com.websudos.phantom.keys.PartitionKey
import org.joda.time.DateTime

/**
 * Mention of focus company
 *
 * @param ticker   ticker of focus company
 * @param source   source of this mention (Twitter, RSS, etc...)
 * @param sourceId source specific id
 * @param time     time
 * @param mentions set of other tickers including focus ticker itself
 */
case class Mention(ticker: Ticker, source: String, sourceId: String, time: DateTime, mentions: Set[Ticker])

sealed class MentionRecord extends CassandraTable[MentionRecord, Mention] with Serializable {

  override val tableName: String = "mention"

  object ticker    extends StringColumn    (this)  with PartitionKey[String]
  object source    extends StringColumn    (this)  with PrimaryKey[String]
  object time      extends DateTimeColumn  (this)  with PrimaryKey[DateTime]
  object source_id extends StringColumn    (this)  with PrimaryKey[String]
  object mentions  extends SetColumn[MentionRecord, Mention, String] (this)

  def fromRow(r: Row): Mention = {
    Mention(Ticker(ticker(r)), source(r), source_id(r), time(r), mentions(r) map Ticker)
  }
}

object MentionRecord extends MentionRecord