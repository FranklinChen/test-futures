name := "test-futures"

organization := "com.franklinchen"

version := "1.0.0"

scalaVersion := "2.10.2-RC2"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.0-RC1" % "test"
)

seq(ScctPlugin.instrumentSettings: _*)
