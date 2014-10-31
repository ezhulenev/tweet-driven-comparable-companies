package com.pellucid.comparables

object Companies {
  def load() = {
    val source = scala.io.Source.fromURL(this.getClass.getResource("/companies.csv"))
    val companies = source.getLines().map(_.split(",")).collect {
      case Array(ticker, name, marketCap) => Company(Ticker(ticker), name, BigDecimal(marketCap))
    }
    companies.toVector.sortBy(_.marketCap).reverse
  }
}