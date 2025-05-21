package com.example.echo

import io.opentelemetry.api.trace.{Span, SpanKind, StatusCode, Tracer}
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.grpc.GrpcClientSettings
import org.apache.pekko.http.scaladsl.model.HttpEntity
import org.apache.pekko.http.scaladsl.model.headers.RawHeader
import org.apache.pekko.stream.scaladsl.Source
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.{ContextPropagators, TextMapSetter}
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.extension.trace.propagation.B3Propagator
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.sentry.Sentry
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import io.grpc.ManagedChannelBuilder

object EchoClient {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    // Initialize Sentry
    initializeSentry()

    // Load configuration with gRPC-specific settings
    val baseConfig = ConfigFactory.load()
    val grpcConfig = ConfigFactory.parseResources("grpc.conf")
    val conf = grpcConfig.withFallback(baseConfig)
    
    // Create client ActorSystem with the combined configuration
    val system = ActorSystem[Nothing](Behaviors.empty[Nothing], "EchoClient", conf)
    implicit val classicSystem = system.classicSystem
    implicit val ec: ExecutionContext = system.executionContext

    // Configure the client with HTTP/2 cleartext (h2c) settings
    logger.info("Creating gRPC client with HTTP/2 cleartext (h2c) to connect to port 8082")
    val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8082)
      .withTls(false)
      .withChannelBuilderOverrides(channelBuilder => 
        channelBuilder
          .usePlaintext()
      )
    
    // Create the client
    val client = EchoServiceClient(clientSettings)

    // Send 10 requests to demonstrate the context issue
    val requests = for (i <- 1 to 20) yield {
      val message = s"Hello$i"
      logger.info(s"Sending request with message: $message")

      // Send the request with trace context
      val requestBuilder = client.echo()

      // Invoke the request
      val response = requestBuilder.invoke(EchoRequest(message))

      response.onComplete {
        case Success(msg) => 
          logger.info(s"[$i] Got response: ${msg.message}")
          Span.current().setAttribute("response.message", msg.message)
        case Failure(e) =>
          logger.error(s"[$i] Error: ${e.getMessage}", e)
          Span.current().setStatus(StatusCode.ERROR, e.getMessage)
      }
      
      response
    }
    
    // Wait for all responses
    import scala.concurrent.Await
    Try {
      val responsesList = requests.toList
      val responses = Future.sequence(responsesList)
      Await.result(responses, 30.seconds)
    }
    
    // Terminate the client
    system.terminate()
  }

  private def initializeSentry(): Unit = {
    Sentry.init(options => {
      options.setDsn("https://b9ca97be3ff8f1cef41dffdcb1e5100b@o447951.ingest.us.sentry.io/4508683222843393")
      options.setEnvironment("dev")
      options.setRelease("echo-client@1.0.0")
      options.setTracesSampleRate(1.0)
      options.setDebug(true)
    })
    
    logger.info("Sentry initialized for client")
  }
} 