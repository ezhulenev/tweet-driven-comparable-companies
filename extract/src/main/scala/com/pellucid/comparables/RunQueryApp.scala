package com.pellucid.comparables

import java.util.concurrent.atomic.AtomicInteger

import com.pellucid.comparables.cassandra.CassandraMentionServiceModule
import com.pellucid.comparables.ingest.MentionsExtractor
import com.pellucid.comparables.schema.Mention
import org.slf4j.LoggerFactory
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{Query, QueryResult, Status, TwitterFactory}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scalaz.concurrent.Task
import scalaz.stream._
import scalaz.stream.io._
import scalaz.syntax.either._


class RunQueryApp extends TwitterAuthorization with Cassandra with CassandraMentionServiceModule {

  implicit class Transformer[+T](fut: => Future[T]) {
    def toTask: Task[T] = {
      Task.async { register => fut.onComplete {
        case Success(v) => register(v.right)
        case Failure(ex) => register(ex.left)
      }
      }
    }
  }

}

object RunQueryApp extends RunQueryApp {
  private val log = LoggerFactory.getLogger(classOf[RunQueryApp])

  val PageSize = 100
  val MaxPages = 10

  // Retry twitter search with these delays between failures
  private val SearchRetries =
    Seq(1.second, 5.seconds, 10.seconds, 30.seconds, 1.minute, 3.minutes, 5.minutes, 15.minutes, 30.minutes, 1.hour, 3.hours)

   def main(args: Array[String]):Unit = {

    // Install Schema into Cassandra
    installSchema()

    // Configure Twitter access
    val auth = new OAuthAuthorization(new ConfigurationBuilder().build())
    val conf = new ConfigurationBuilder().setUseSSL(true).build()
    val twitter = new TwitterFactory(conf).getInstance(auth)

     val searchForTweets = channel[Company, Vector[Status]] {
       company =>
         log.info(s"Search Tweets for ticker: ${company.ticker.value}. Company name: ${company.name}")

         def buildQuery(maxId: Long = -1l) = {
           val query = new Query(s"$$${company.ticker.value}")
           query.setCount(PageSize)
           query.setMaxId(maxId)
           query
         }

         def q(query: Query) = {
           val attempt = new AtomicInteger(0)
           Task.delay {
             log.debug(s"Search query: ${query.getQuery}. MaxId: ${query.getMaxId}. Attempt: ${attempt.incrementAndGet()}")
             twitter.search(query)
           } retry SearchRetries
         }

         val query = scalaz.stream.async.signal[Query]
         val nextQuery: Sink[Task, QueryResult] =
           Process.constant((qr: QueryResult) => qr.getTweets.to[Vector].lastOption match {
             case None => query.close
             case Some(status) => query.set(buildQuery(status.getId))
           })

         val queryResults = (Process.eval(q(buildQuery())) ++ query.discrete.evalMap(q).take(MaxPages)) observe nextQuery
         val task = queryResults.map(_.getTweets.asScala).runLog.map(_.flatten.toVector)

         task
     }

    val saveToCassandra: Sink[Task, Vector[Mention]] = channel[Vector[Mention], Unit] {
      mentions =>
        log.debug(s"Save ${mentions.size} mentions to Cassandra")
        Future.sequence(mentions map mentionService.insert).map(_ => ()).toTask
    }

    val companies: Process[Task, Company] =
      Process.emitAll(Companies.load())

     try {

       // Search Tweets & save them to Cassandra
       companies.through(searchForTweets).
         map(_.map(implicitly[MentionsExtractor[Status]].mentions).flatten).
         to(saveToCassandra).run.run

     } catch {
       case err: Throwable =>
         log.error(s"Failed to fetch mentions from Twitter. Error: $err", err)
         System.exit(1)
     }

     System.exit(1)
  }
}