package com.example.echo

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.{HttpRequest, HttpResponse}
import org.apache.pekko.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory
import io.sentry.Sentry
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import org.apache.pekko.http.scaladsl.Http.ServerBinding

object EchoServer {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    // Initialize Sentry
    initializeSentry()
    
    // Load configuration with gRPC-specific settings
    val baseConfig = ConfigFactory.load()
    val grpcConfig = ConfigFactory.parseResources("grpc.conf")
    val conf = grpcConfig.withFallback(baseConfig)
    
    // Create the ActorSystem with the combined configuration
    val system = ActorSystem[Nothing](Behaviors.empty, "EchoServer", conf)
    new EchoServer(system).run()
  }
  
  private def initializeSentry(): Unit = {
    Sentry.init(options => {
      options.setDsn("https://b9ca97be3ff8f1cef41dffdcb1e5100b@o447951.ingest.us.sentry.io/4508683222843393")
      options.setEnvironment("dev")
      options.setRelease("echo-service@1.0.0")
      options.setTracesSampleRate(1.0)
      options.setDebug(true)
    })
    
    logger.info("Sentry initialized")
  }
}

class EchoServer(system: ActorSystem[_]) {
  private val logger = LoggerFactory.getLogger(getClass)
  
  def run(): Future[ServerBinding] = {
    implicit val sys = system
    implicit val ec: ExecutionContext = system.executionContext

    // Create service implementation with our header interceptor
    val serviceImpl = new EchoServiceImpl(system)
    
    // Create service with header interceptor
    val service: HttpRequest => Future[HttpResponse] =
      EchoServicePowerApiHandler.withServerReflection(serviceImpl)

    // Use the configuration from application.conf with HTTP/2 enabled
    val serverSettings = ServerSettings(system)
    
    // Configure server for HTTP/2 cleartext (h2c) with prior knowledge mode - critical for gRPC
    logger.info("Starting EchoServer with HTTP/2 cleartext (h2c) on port 8082")
    val bound = Http().newServerAt("127.0.0.1", 8082)
      .withSettings(serverSettings)
      // Use HTTP/2 with prior knowledge mode (h2c)
      .bind(service)
      .map(_.addToCoordinatedShutdown(3.seconds))

    bound.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        logger.info(s"EchoServer online at http://${address.getHostString}:${address.getPort}/ with HTTP/2 cleartext enabled")
        logger.info(s"Using HTTP/2 with prior knowledge mode (h2c)")
      case Failure(ex) =>
        logger.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }

    bound
  }
} 