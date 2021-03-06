name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLogback).settings(
  libraryDependencies ++= Seq(
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.8.2",
    "org.apache.logging.log4j" % "log4j-api" % "2.8.2",
    "org.apache.logging.log4j" % "log4j-core" % "2.8.2"
  )
)

libraryDependencies += "org.apache.logging.log4j" %% "log4j-api-scala" % "2.8.2"

scalaVersion := "2.11.11"

libraryDependencies += jdbc
libraryDependencies += cache
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.42"
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.1"
libraryDependencies += evolutions