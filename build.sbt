name := "Fs2Helper"

scalaVersion := "2.12.6"

organization := "scalavision"

version := "0.1.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "0.10.4",
  "co.fs2" %% "fs2-io" % "0.10.4"
)
