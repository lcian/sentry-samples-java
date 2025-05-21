package com.example.echo

import akka.actor.typed.ActorSystem
import io.grpc.Status
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.{Span, SpanKind}
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.{TextMapGetter, TextMapPropagator}
import io.sentry.Sentry
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal
import akka.grpc.{GrpcServiceException => AkkaGrpcServiceException}
import akka.grpc.scaladsl.Metadata

// Custom exception to use in our example
class GrpcServiceException(val status: Status) extends RuntimeException(status.getDescription)

class EchoServiceImpl(system: ActorSystem[_]) extends EchoServicePowerApi {
  private val logger = LoggerFactory.getLogger(getClass)
  private implicit val ec: ExecutionContext = system.executionContext

  private val propagator: TextMapPropagator = GlobalOpenTelemetry.getPropagators.getTextMapPropagator

  private val metadataGetter = new TextMapGetter[Metadata] {
    override def keys(carrier: Metadata): java.lang.Iterable[String] = {
      val keysIterable = carrier.asList.map(_._1).asJava
      logger.info(s"Metadata keys: ${keysIterable.asScala.mkString(", ")}")
      keysIterable
    }

    override def get(carrier: Metadata, key: String): String = {
      val v = carrier.getText(key).getOrElse("")
      logger.info(s"Getting key: $key; value: $v")
      v
    }
  }

  // Required implementation from the EchoService trait
  override def echo(in: EchoRequest, metadata: Metadata): Future[EchoResponse] = {
    logger.info("Received echo request: " + in.message)
    val parentContext: Context = propagator.extract(Context.current(), metadata, metadataGetter)
    val tracer = GlobalOpenTelemetry.getTracer("customer.common.echo")
    val parentSpan = tracer.spanBuilder("server-span").setParent(parentContext).setSpanKind(SpanKind.SERVER).startSpan()
    val parentScope = parentSpan.makeCurrent()

    Sentry.getSpan().setData("sentry-span-echoMessage", in.message)
    Sentry.configureScope { scope =>
      scope.setContexts("sentry-scope-echoMessage", in.message)
    }

    val capturedContext: Context = Context.current()

    val f = Future {
      val scope1 = capturedContext.makeCurrent()
      val childSpan  = tracer.spanBuilder("EchoService.echo.reverse").startSpan()
      val childScope = childSpan.makeCurrent()

      try {
        val reversed = true
        val message = if (reversed) in.message.reverse else in.message

        childSpan.setAttribute("otel-attribute-echoReply", s"${message}")
        Sentry.getSpan().setData("sentry-span-echoReply", message)
        Sentry.configureScope { scope =>
          scope.setContexts("sentry-scope-echoReply", message)
        }

        if (message.contains("4"))
          throw new AkkaGrpcServiceException(status =
            Status.RESOURCE_EXHAUSTED.withDescription(s"Sentry test error. $message=$message;in.message=${in.message}"))

        EchoResponse(message, System.currentTimeMillis())
      } catch {
        case NonFatal(e) =>
          logger.error(s"FAILURE for ${in.message}: ${e.getMessage}", e)
          Sentry.captureException(e)
          throw e
      } finally {
        childSpan.end()
        childScope.close()
        scope1.close()
      }
    }

    f.onComplete { _ =>
      Span.current().setAttribute("otel-attribute-echoMessage-onComplete", s"${in.message}")
      Sentry.getSpan().setData("sentry-span-echoMessage-onComplete", in.message)
      Sentry.configureScope { scope =>
        scope.setContexts("sentry-scope-echoMessage-onComplete", in.message)
      }

      parentSpan.end()
      parentScope.close()
    }

    f
  }
}
