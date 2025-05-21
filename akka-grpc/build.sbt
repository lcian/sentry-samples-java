name := "akka-grpc-quickstart-scala"

version := "1.0"
scalaVersion := "2.13.15"

val akkaVersion = "2.10.5"
lazy val akkaGrpcVersion = sys.props.getOrElse("akka-grpc.version", "2.5.5")
val openTelemetryVersion = "1.40.0"

enablePlugins(AkkaGrpcPlugin)
enablePlugins(JavaAgent)

// Enable Power API generation for accessing metadata
akkaGrpcCodeGeneratorSettings += "server_power_apis"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-pki" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.5.18",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  
  // OpenTelemetry dependencies
  "io.opentelemetry" % "opentelemetry-api" % openTelemetryVersion,
  "io.opentelemetry" % "opentelemetry-sdk" % openTelemetryVersion,
  "io.opentelemetry" % "opentelemetry-sdk-trace" % openTelemetryVersion,
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % openTelemetryVersion,
  "io.opentelemetry" % "opentelemetry-extension-trace-propagators" % openTelemetryVersion,
  
  // Sentry dependency
  "io.sentry" % "sentry" % "8.12.0",
  //"io.sentry" % "sentry-opentelemetry-bootstrap" % "8.12.0" % Provided,
  //"io.sentry" % "sentry-opentelemetry-core" % "8.12.0" % Provided,
  //"io.sentry" % "sentry-opentelemetry-agent" % "8.12.0"
)

//javaAgents += "io.sentry" % "sentry-opentelemetry-agent" % "8.11.1"
//javaOptions ++= Seq(
//  s"-javaagent:${baseDirectory.value}/sentry-opentelemetry-agent-8.12.0.jar"
//)
//javaOptions += "-Dotel.javaagent.debug=true" //Debug OpenTelemetry Java agent 

fork := true
