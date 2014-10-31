package com.pellucid.comparables.ingest

import java.util.Date

import com.pellucid.comparables.Ticker
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import twitter4j.Status

class MentionsExtractorSpec extends FlatSpec with MockFactory {

  private def statusWithText(text: String): Status = {
    val status = mock[Status]
    (status.getText _).expects().returning(text)
    (status.getId _).expects().returning(0l)
    (status.getCreatedAt _).expects().returning(new Date())
    status
  }

  "MentionsExtractor" should "extract all mentions from twitter status" in {
    val status = statusWithText("$FB Entering A New Industry http://stks.co/s0r2k")
    val mentions = implicitly[MentionsExtractor[Status]].mentions(status)
    assert(mentions.size == 1)
    assert(mentions.head.ticker == Ticker("FB"))
  }

  it should "extract multiple tickers" in {
    val status = statusWithText("$FB News: Google Mobile Messenger: A Dead On Arrival Product $FB $TWTR $GOOGL ...")
    val mentions = implicitly[MentionsExtractor[Status]].mentions(status)
    assert(mentions.size == 3)
    val tickers = mentions.map(_.ticker)
    assert(tickers.contains(Ticker("FB")))
    assert(tickers.contains(Ticker("TWTR")))
    assert(tickers.contains(Ticker("GOOGL")))
  }
}

