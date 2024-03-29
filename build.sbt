import Dependencies._
enablePlugins(GhpagesPlugin)
enablePlugins(SiteScaladocPlugin)

ThisBuild / scalaVersion     := "2.12.9"
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

git.remoteRepo := "git@github.com:dagobuh/dagobuh.git"
paradoxTheme := Some(builtinParadoxTheme("generic"))

val supportedScalaVersions = Seq("2.11.12", "2.12.9")

scalacOptions += "-Ypartial-unification"

val common = Seq(
  scalaTest % Test,
  "org.typelevel" %% "cats-core" % "2.0.0-RC1"
)

lazy val root = (project in file("."))
  .settings(
    name := "dagobuh",
    skip in publish := true,
    crossScalaVersions := supportedScalaVersions,
  )
  .aggregate(dagobuhApi, dagobuhFlink, dagobuhList)

lazy val dagobuhApi = (project in file("dagobuh-api"))
  .settings(
    name := "dagobuh-api",
    libraryDependencies ++= common,
    crossScalaVersions := supportedScalaVersions,
  )

val flinkDeps = Seq(
  "org.apache.flink" %% "flink-scala" % "1.8.1+",
  "org.apache.flink" %% "flink-streaming-scala" % "1.8.1+"
) ++ common

lazy val dagobuhFlink = (project in file("dagobuh-flink"))
  .settings(
    name := "dagobuh-flink",
    libraryDependencies ++= flinkDeps,
    crossScalaVersions := supportedScalaVersions,
  ).dependsOn(dagobuhApi % "compile->compile")

lazy val dagobuhList = (project in file("dagobuh-list"))
  .settings(
    name := "dagobuh-list",
    libraryDependencies ++= common,
    crossScalaVersions := supportedScalaVersions,
  ).dependsOn(dagobuhApi % "compile->compile")
