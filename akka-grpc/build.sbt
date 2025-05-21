name := "pekko-grpc-quickstart-scala"

version := "1.0"
scalaVersion := "2.13.15"

val pekkoVersion = "1.1.2"
lazy val pekkoGrpcVersion = sys.props.getOrElse("pekko-grpc.version", "1.1.1")
val openTelemetryVersion = "1.40.0"

enablePlugins(PekkoGrpcPlugin)
enablePlugins(JavaAgent)

// Enable Power API generation for accessing metadata
pekkoGrpcCodeGeneratorSettings += "server_power_apis"

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

resolvers += Resolver.mavenCentral

libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
  "org.apache.pekko" %% "pekko-discovery" % pekkoVersion,
  "org.apache.pekko" %% "pekko-pki" % pekkoVersion,
  "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion,
  "ch.qos.logback" % "logback-classic" % "1.5.18",
  "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion % Test,
  "org.apache.pekko" %% "pekko-stream-testkit" % pekkoVersion % Test,
  
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
