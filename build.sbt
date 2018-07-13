name := "Fs2Helper"

scalaVersion := "2.12.6"

organization := "scalavision"

version := "0.1.2-SNAPSHOT"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "1.0.0-M1",
  "co.fs2" %% "fs2-io" % "1.0.0-M1"
)

assemblyJarName in assembly := "fs2helper.jar"
