package com.pellucid.comparables

import org.scalatest.FlatSpec

class CompaniesSpec extends FlatSpec {

  "Companies object" should "load all companies from .csv" in {
    val companies = Companies.load()
    assert(companies.size > 100)
  }

}
