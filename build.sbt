ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "spotify-client"
  )

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M18",
  "io.circe" %% "circe-core" % "0.14.7",
  "io.circe" %% "circe-generic" % "0.14.7",
  "io.circe" %% "circe-parser" % "0.14.10",
  "org.scalafx" %% "scalafx" % "22.0.0-R33"
)

enablePlugins(AssemblyPlugin)

assembly / mainClass := Some("Main")
assembly / assemblyJarName := "spotify-cli-client.jar"

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
