
object Dependencies {

  import Dependency._

  private[Dependencies] val testing = Seq(Test.scalaTest, Test.scalaMock)

  val common =
    Seq(
      config, phantomDsl, phantomZookeeper
    ) ++ testing ++ Seq(Test.phantomTesting)

  val extract =
    Seq(
      config, protobuf,
      sparkCore, sparkStreaming, sparkStreamingTwitter, sparkSql, sparkCassandraConnector,
      scalazCore, scalazStream
    ) ++ testing ++ Seq(Test.phantomTesting)

  val suggest =
    Seq(
      akkaActor, sprayCan, sprayRouting, sprayJson
    )

}
