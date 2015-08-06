lazy val root = (project in file("."))
organization  := "juan85"
name:= "curiouscomapnion"
version       := "0.1"

scalaVersion  := "2.11.7"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

resolvers += "Brando Repository" at "http://chrisdinn.github.io/releases/"

libraryDependencies ++= {
  val akkaV  = "2.3.10"
  val sprayV = "1.3.3"
  val kamonV = "0.3.5"
  Seq(
    "com.typesafe.akka" %% "akka-http-experimental" % "1.0-RC3",
    "org.specs2" %% "specs2" % "2.3.12" % "test",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV,
    "com.typesafe.akka" %% "akka-persistence-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    
      // redis connector
  	"com.digital-achiever" %% "brando" % "3.0.0",
  	//testing
    "org.scalatest" % "scalatest_2.11" % "3.0.0-M7",
    // scalacheck
    "org.scalacheck" % "scalacheck_2.11" % "1.12.4",
    "ch.qos.logback" % "logback-classic" % "1.1.3"
    
  	
    
  )
}

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")


Revolver.settings
