import sbt._, Keys._

object TestSettings {

  def testSettings: Seq[Def.Setting[_]] = Seq(
    // Disable logging in all tests
    javaOptions in Test += "-Dlogback.configurationFile=disable.logs.xml",
    // Generate JUnit test reports
    testOptions in Test <+= (target in Test) map {
      t => Tests.Argument(TestFrameworks.ScalaTest, "-u", (t / "test-reports").toString)
    }
  )

  def integrationTestSettings: Seq[Def.Setting[_]] = Defaults.itSettings ++ Seq(
    // Disable logging in all tests
    javaOptions in IntegrationTest += "-Dlogback.configurationFile=disable.logs.xml",
    // Generate JUnit test reports
    testOptions in IntegrationTest <+= (target in IntegrationTest) map {
      t => Tests.Argument(TestFrameworks.ScalaTest, "-u", (t / "test-reports").toString)
    }
  )
}