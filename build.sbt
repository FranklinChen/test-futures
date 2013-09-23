name := "test-futures"

organization := "com.franklinchen"

organizationHomepage := Some(url("http://franklinchen.com/"))

homepage := Some(url("http://github.com/FranklinChen/test-futures"))

startYear := Some(2013)

description := "Test Scala futures"

version := "1.0.0"

scalaVersion := "2.10.2"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.2.2" % "test"
)
