import AssemblyKeys._

fork in IntegrationTest := true

assemblySettings

net.virtualvoid.sbt.graph.Plugin.graphSettings

jarName in assembly := "suggest-comparables.jar"

mainClass in assembly := Some("com.pellucid.comparables.SuggestionServer")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case x if x.startsWith("META-INF/ECLIPSEF.RSA") => MergeStrategy.last
  case x if x.startsWith("META-INF/mailcap") => MergeStrategy.last
  case x if x.endsWith("plugin.properties") => MergeStrategy.last
  case x if x.endsWith("pom.properties") => MergeStrategy.last
  case x if x.endsWith("pom.xml") => MergeStrategy.last
  case x if x.endsWith("cmdline.arg.info.txt.1") => MergeStrategy.last
  case x => old(x)
}
}

libraryDependencies ++= Dependencies.suggest
