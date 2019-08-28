import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "org.dagobuh"
ThisBuild / organizationName := "Dagobuh"

scalacOptions += "-Ypartial-unification"

val common = Seq(
  scalaTest % Test
)

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
