import sbt._


object Dependency {

  // Versions
  object V {
    val Config             = "1.2.1"

    val Protobuf           = "2.5.0"

    val Scalaz             = "7.1.0"
    val ScalazStream       = "0.5a"

    val Akka               = "2.3.4"
    val Spray              = "1.3.1"
    val SprayJson          = "1.2.6"

    val Spark              = "1.1.0"
    val SparkCassandra     = "1.1.0-beta1"

    val Phantom            = "1.2.2"

    // Test libraries
    val ScalaTest          = "2.2.1"
    val ScalaMock          = "3.1.RC1"
  }

  // Compile

  val config                  = "com.typesafe"         % "config"                     % V.Config

  val protobuf                = "com.google.protobuf"  % "protobuf-java"              % V.Protobuf

  val scalazCore              = "org.scalaz"         %% "scalaz-core"                 % V.Scalaz
  val scalazStream            = "org.scalaz.stream"  %% "scalaz-stream"               % V.ScalazStream


  val akkaActor               = "com.typesafe.akka"   %%  "akka-actor"                % V.Akka
  val sprayCan                = "io.spray"            %%   "spray-can"                % V.Spray
  val sprayRouting            = "io.spray"            %%   "spray-routing-shapeless2" % V.Spray
  val sprayJson               = "io.spray"            %%   "spray-json"               % V.SprayJson

  val phantomDsl              = "com.websudos"        %% "phantom-dsl"                % V.Phantom        excludeSparkDependencies()
  val phantomZookeeper        = "com.websudos"        %% "phantom-zookeeper"          % V.Phantom        excludeSparkDependencies()

  val sparkCore               = "org.apache.spark"    %% "spark-core"                 % V.Spark          excludeSparkDependencies()
  val sparkStreaming          = "org.apache.spark"    %% "spark-streaming"            % V.Spark          excludeSparkDependencies()
  val sparkStreamingTwitter   = "org.apache.spark"    %% "spark-streaming-twitter"    % V.Spark          excludeSparkDependencies()
  val sparkSql                = "org.apache.spark"    %% "spark-sql"                  % V.Spark          excludeSparkDependencies()
  val sparkCassandraConnector = "com.datastax.spark"  %% "spark-cassandra-connector"  % V.SparkCassandra excludeSparkDependencies()

  implicit class RichModuleId(m: ModuleID) {

    def excludeSparkDependencies() =
      m.
        exclude("commons-beanutils", "commons-beanutils-core").
        exclude("commons-collections", "commons-collections").
        exclude("commons-logging", "commons-logging").
        exclude("org.slf4j", "slf4j-log4j12").
        exclude("org.hamcrest", "hamcrest-core").
        exclude("junit", "junit").
        exclude("org.jboss.netty", "netty").
        exclude("com.esotericsoftware.minlog", "minlog")
  }


  // Test

  object Test {

    val scalaTest         =   "org.scalatest"        %% "scalatest"                   % V.ScalaTest     % "test"
    val scalaMock         =   "org.scalamock"        %% "scalamock-scalatest-support" % V.ScalaMock     % "test"

    val phantomTesting    =   "com.websudos"         %% "phantom-testing"             % V.Phantom       % "test,it" excludeSparkDependencies()
  }
}
