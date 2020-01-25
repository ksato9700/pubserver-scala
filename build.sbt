name := """pubserver"""
organization := "com.ksato9700"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.10"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  "net.logstash.logback" % "logstash-logback-encoder" % "6.3",
  "io.lemonlabs" %% "scala-uri" % "1.5.1",
  "net.codingwell" %% "scala-guice" % "4.2.6"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.ksato9700.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.ksato9700.binders._"
