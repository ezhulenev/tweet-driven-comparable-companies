import sbt._, Keys._

object Publish {

  val baseVersion = Def.settingKey[String]("the base of the version string")
  val isRelease = Def.settingKey[Boolean]("True if this is a release")
  val gitHeadCommitSha = Def.settingKey[String]("The git short SHA of HEAD")

  private def getPublishTo(release: Boolean) = {
    val artifactory = "http://pellucid.artifactoryonline.com/pellucid/"
    if (!release)
      Some("lib-snapshots-local" at artifactory + "libs-snapshots-local")
    else
      Some("lib-releases-local" at artifactory + "libs-releases-local")
  }

  private def getVersion(baseVersion: String, release: Boolean, sha: String) = {
    import java.util.{Calendar, TimeZone}
    import java.text.SimpleDateFormat
    val utcTz = TimeZone.getTimeZone("UTC")
    val cal = Calendar.getInstance(utcTz)
    val sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
    sdf.setCalendar(cal)
    if (!release) s"$baseVersion-${sdf.format(cal.getTime)}-$sha" else baseVersion
  }

  def buildSettings: Seq[Def.Setting[_]] = Seq(
    isRelease in ThisBuild := sys.props("release") == "true",
    gitHeadCommitSha in ThisBuild := Process("git rev-parse --short HEAD").lines.head,
    version in ThisBuild := getVersion(baseVersion.value, isRelease.value, gitHeadCommitSha.value),
    publishTo in ThisBuild := getPublishTo(isRelease.value),
    publishMavenStyle in ThisBuild := true
  )
}
