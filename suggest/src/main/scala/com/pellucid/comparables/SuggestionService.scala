package com.pellucid.comparables

import com.pellucid.comparables.cassandra.CassandraService
import com.pellucid.comparables.schema.{Recommendation, RecommendationRecord}
import spray.http.StatusCodes._
import spray.json._
import spray.routing._
import spray.util.LoggingContext


object SuggestionJsonProtocol extends DefaultJsonProtocol {

  implicit val tickerFormat = new RootJsonFormat[Ticker] {
    override def write(obj: Ticker): JsValue = JsString(obj.value)

    override def read(json: JsValue): Ticker = json match {
      case JsString(ticker) => Ticker(ticker)
      case _ => deserializationError(s"Ticker expected")
    }
  }

  implicit val companyFormat = jsonFormat3(Company)

  implicit val recommendationFormat = jsonFormat4(Recommendation)

}

class SuggestionService extends HttpServiceActor with CassandraService { module =>

  implicit def myExceptionHandler(implicit log: LoggingContext) =
    ExceptionHandler { case e: Throwable =>
      log.error(s"Service request failed: $e", e)
      requestUri { uri => complete(InternalServerError, e.getMessage) }
    }

  private lazy val companies = Companies.load()

  import com.pellucid.comparables.SuggestionJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  protected val CompanyTicker: PathMatcher1[Ticker] = PathMatcher(".+".r) flatMap {
    string ⇒ Some(Ticker(string))
  }

  val receive = runRoute {
    path("companies") {
      get {
        complete {
          companies
        }
      }
    } ~
    path("comparables" / CompanyTicker) { ticker =>
      parameters('n.as[Int] ? 10) { (n) =>
        get {
          complete {
            import com.websudos.phantom.Implicits._
            RecommendationRecord.select.
              where(_.ticker eqs ticker.value).
              and(_.position lt n.toLong).fetch()
          }
        }
      }
    }
  }

}
