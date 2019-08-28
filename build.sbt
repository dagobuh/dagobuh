import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.2"
ThisBuild / organization     := "org.dagobuh"
ThisBuild / organizationName := "Dagobuh"

ThisBuild / organizationHomepage := Some(url("https://dagobuh.org"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/dagobuh/dagobuh"),
    "scm:git@github.com:dagobuh/dagobuh.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "tdbgamer",
    name  = "Timothy Bess",
    email = "tdbgamer@gmail.com ",
    url   = url("https://github.com/dagobuh/dagobuh")
  )
)

ThisBuild / description := "Streaming Graph DSL with backends for (flink, etc.)"
ThisBuild / licenses := List("MPL-2.0" -> new URL("https://mozilla.org/MPL/2.0/"))
ThisBuild / homepage := Some(url("https://github.com/dagobuh/dagobuh"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

useGpg := true

scalacOptions += "-Ypartial-unification"

val common = Seq(
  scalaTest % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "dagobuh",
    skip in publish := true,
  )
  .aggregate(dagobuhApi, dagobuhFlink, dagobuhList)

lazy val dagobuhApi = (project in file("dagobuh-api"))
  .settings(
    name := "dagobuh-api",
    libraryDependencies ++= common,
  )

val flinkDeps = Seq(
  "org.apache.flink" %% "flink-scala" % "1.8+",
  "org.apache.flink" %% "flink-streaming-scala" % "1.8+"
) ++ common

lazy val dagobuhFlink = (project in file("dagobuh-flink"))
  .settings(
    name := "dagobuh-flink",
    libraryDependencies ++= flinkDeps,
  ).dependsOn(dagobuhApi % "compile->compile")

lazy val dagobuhList = (project in file("dagobuh-list"))
  .settings(
    name := "dagobuh-list",
    libraryDependencies ++= common,
  ).dependsOn(dagobuhApi % "compile->compile")
