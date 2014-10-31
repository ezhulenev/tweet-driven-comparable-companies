organization in ThisBuild := "com.pellucid.comparables"

Publish.baseVersion in ThisBuild := "0.0.1"

scalaVersion in ThisBuild := "2.10.4"

scalacOptions in ThisBuild ++= Seq("-deprecation", "-feature", "-unchecked")

shellPrompt in ThisBuild := ShellPrompt.buildShellPrompt

resolvers in ThisBuild ++= Seq(
  "Pellucid Bintray"          at "http://dl.bintray.com/content/pellucid/maven",
  "Local Maven Repository"    at "file://"+Path.userHome.absolutePath+"/.m2/repository",
  "Typesafe Repo"             at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype Snapshots"        at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases"         at "http://oss.sonatype.org/content/repositories/releases",
  "Scalaz Bintray Repo"       at "http://dl.bintray.com/scalaz/releases"
)

// Default project definition

def ComparablesProject(name: String) = {
  Project(id = name, base = file(name)).
    // Add Integration Tests
    configs(IntegrationTest).
    settings(TestSettings.testSettings:_*).
    settings(TestSettings.integrationTestSettings:_*)
}

lazy val common = ComparablesProject("common")

lazy val extract = ComparablesProject("extract").
  dependsOn(common)

lazy val suggest = ComparablesProject("suggest").
  dependsOn(common)









