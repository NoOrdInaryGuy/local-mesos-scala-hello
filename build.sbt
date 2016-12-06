name := "local-mesos-scala-hello"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

// Fork to avoid native library classloader issues when invoking "run" repeatedly in SBT
val jvmOpts = Seq("-Djava.library.path=" + System.getProperty("java.library.path"))
fork in run := true
javaOptions in run ++= jvmOpts

libraryDependencies ++= Seq(
  "org.apache.mesos" % "mesos" % "1.1.0"
)
