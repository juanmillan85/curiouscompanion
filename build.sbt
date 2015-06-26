organization  := "juan85"

version       := "0.1"
lazy val root = (project in file("."))
scalaVersion  := "2.11.6"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies ++= {
  val akkaV  = "2.3.10"
  val sprayV = "1.3.3"
  val kamonV = "0.3.5"
  Seq(
    "com.typesafe.akka" %% "akka-http-experimental" % "1.0-RC3",
    "org.specs2" %% "specs2" % "2.3.12" % "test",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV,
    "com.typesafe.akka" %% "akka-persistence-experimental" % akkaV
  )
}

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")


Revolver.settings
