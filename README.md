# Comparable Companies Recommendation

### Building comparable companies recommendations from Twitter live stream with Spark, Spark Streaming and Cassandra

It's code for blog post: http://eugenezhulenev.com/blog/2014/11/20/twitter-analytics-with-spark/

## Testing

Integration tests starts embedded Cassandra instance

    sbt test
    sbt it:test

## Building

In the root directory run:

    sbt assembly

The application fat jars will be placed in:
  - `comparables/extract/target/scala-2.10/extract-comparables.jar`
  - `comparables/suggest/target/scala-2.10/suggest-comparables.jar`


## Running

### Build recommendations

To run `extract` application you need to prepare valid application.conf with configured Cassandra and Spark access, and Twitter authorization

    cassandra {

      keyspace = "comparables"
      host = "cassandra-host"

      nativePort = 9042
      rpcPort = 9160
    }

    spark {
      master = "spark://spark-host:7077"
    }

    twitter4j.oauth {
      consumerKey = ""
      consumerSecret = ""
      accessToken = ""
      accessTokenSecret = ""
    }


and then run java cmd

    java -Dconfig.file=path-to-application-conf -cp extract-comparables.jar com.pellucid.comparables.RunStreamingApp


### Suggest comparable company

To run `suggest` application you need to prepare valid application.conf with configured Cassandra access

    cassandra {

      keyspace = "comparables"
      host = "cassandra-host"

      nativePort = 9042
      rpcPort = 9160
    }

and then run java cmd

    java -Dconfig.file=path-to-application-conf -jar suggest-comparables.jar

