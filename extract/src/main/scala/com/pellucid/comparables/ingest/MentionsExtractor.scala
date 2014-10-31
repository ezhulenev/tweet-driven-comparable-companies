package com.pellucid.comparables.ingest

import com.pellucid.comparables.{Companies, Ticker}
import com.pellucid.comparables.schema.Mention
import org.joda.time.DateTime
import twitter4j.Status

trait MentionsExtractor[T] extends Serializable {
  def mentions(obj: T): Set[Mention]
}

object MentionsExtractor {

  private val CompaniesTickers = Companies.load().map(_.ticker).toSet

  assume(CompaniesTickers.nonEmpty, s"Companies reference data is not available")

  implicit object TwitterStatusTickersExtractor extends MentionsExtractor[Status] {

    private val Source = "Twitter"
    private val r = "\\$([A-Z]){1,6}".r

    private def parseTickers(status: Status) = {
      val text = status.getText
      r.findAllIn(text).map(_.filter(_ != '$')).map(Ticker).toSet
    }

    def mentions(obj: Status) = {
      val statusId = obj.getId.toString
      val createdAt = new DateTime(obj.getCreatedAt)

      val tickers = parseTickers(obj).filter(CompaniesTickers.contains)
      tickers map (ticker => Mention(ticker, Source, statusId, createdAt, tickers))
    }
  }

}

