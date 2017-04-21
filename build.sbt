enablePlugins(GatlingPlugin)

name := "akka-benchmark"

version := "1.0"

scalaVersion := "2.11.11"

resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % "2.5.0",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.0",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.5.0",
  "com.typesafe.akka" %% "akka-persistence" % "2.5.0",
  "com.typesafe.akka" %% "akka-remote" % "2.5.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.0",
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "org.json4s" %% "json4s-jackson" % "3.5.0",
  "de.heikoseeberger" %% "akka-http-json4s" % "1.11.0",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.0.0",
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.21"
)

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.4" % "test,it"
libraryDependencies += "io.gatling" % "gatling-test-framework"  % "2.2.4" % "test,it"
