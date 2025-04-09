package dev.lcian

import io.grpc.ManagedChannelBuilder
import kotlinx.serialization.Serializable
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import config.GalaxyServiceGrpc
import config.NebulaServiceGrpc
import config.QuasarServiceGrpc
import config.WormholeServiceGrpc
import config.Config.GetGalaxiesRequest
import config.Config.CosmosRequest
import config.Config.GalaxiesResponse
import config.Config.Galaxy
import config.Config.Nebula
import config.Config.Quasar
import config.Config.Wormhole
import io.grpc.stub.StreamObserver
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory
import io.grpc.ClientInterceptor
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.MethodDescriptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.ForwardingClientCallListener
// Add Kotlin gRPC stub imports
import config.GalaxyServiceGrpcKt
import config.NebulaServiceGrpcKt
import config.QuasarServiceGrpcKt
import config.WormholeServiceGrpcKt

private val logger = LoggerFactory.getLogger("dev.lcian.Routing")

// Define a gRPC interceptor to log request details
class LoggingInterceptor : ClientInterceptor {
    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        val startTime = System.currentTimeMillis()
        val methodName = method.fullMethodName
        logger.info("gRPC Request started: $methodName")
        
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun sendMessage(message: ReqT) {
                logger.info("gRPC Request payload for $methodName: $message")
                super.sendMessage(message)
            }

            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                val listener = object : ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    override fun onMessage(message: RespT) {
                        val duration = System.currentTimeMillis() - startTime
                        logger.info("gRPC Response received for $methodName after ${duration}ms: $message")
                        super.onMessage(message)
                    }

                    override fun onClose(status: io.grpc.Status, trailers: Metadata) {
                        val duration = System.currentTimeMillis() - startTime
                        if (status.isOk) {
                            logger.info("gRPC Call $methodName completed successfully in ${duration}ms")
                        } else {
                            logger.error("gRPC Call $methodName failed in ${duration}ms: ${status.code} - ${status.description}")
                        }
                        super.onClose(status, trailers)
                    }
                }
                super.start(listener, headers)
            }
        }
    }
}

fun Application.configureRouting() {
    // Install StatusPages to handle exceptions
    install(StatusPages) {
        exception<StatusRuntimeException> { call, cause ->
            logger.error("gRPC error", cause)
            call.respondText(
                text = "Service unavailable: ${cause.message}",
                status = HttpStatusCode.ServiceUnavailable
            )
        }
        exception<Throwable> { call, cause ->
            logger.error("Unexpected error", cause)
            call.respondText(
                text = "Internal server error: ${cause.message}",
                status = HttpStatusCode.InternalServerError
            )
        }
    }
    
    // Create a logging interceptor to track gRPC requests
    val loggingInterceptor = LoggingInterceptor()
    
    // Create the gRPC channels for each service
    val galaxyChannel = ManagedChannelBuilder.forAddress("localhost", 9091)
        .usePlaintext()
        .intercept(loggingInterceptor)
        .build()
    
    val nebulaChannel = ManagedChannelBuilder.forAddress("localhost", 9092)
        .usePlaintext()
        .intercept(loggingInterceptor)
        .build()
    
    val quasarChannel = ManagedChannelBuilder.forAddress("localhost", 9093)
        .usePlaintext()
        .intercept(loggingInterceptor)
        .build()
    
    val wormholeChannel = ManagedChannelBuilder.forAddress("localhost", 9094)
        .usePlaintext()
        .intercept(loggingInterceptor)
        .build()

    // Initialize the Kotlin stubs
    val galaxyStub = GalaxyServiceGrpcKt.GalaxyServiceCoroutineStub(galaxyChannel)
    val nebulaStub = NebulaServiceGrpcKt.NebulaServiceCoroutineStub(nebulaChannel)
    val quasarStub = QuasarServiceGrpcKt.QuasarServiceCoroutineStub(quasarChannel)
    val wormholeStub = WormholeServiceGrpcKt.WormholeServiceCoroutineStub(wormholeChannel)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/config") {
            try {
                getConfig(call, galaxyStub, nebulaStub, quasarStub, wormholeStub)
            } catch (e: Exception) {
                logger.error("Error in /config endpoint", e)
                throw e
            }
        }
    }
}

suspend fun getConfig(
    call: ApplicationCall,
    galaxyStub: GalaxyServiceGrpcKt.GalaxyServiceCoroutineStub,
    nebulaStub: NebulaServiceGrpcKt.NebulaServiceCoroutineStub,
    quasarStub: QuasarServiceGrpcKt.QuasarServiceCoroutineStub,
    wormholeStub: WormholeServiceGrpcKt.WormholeServiceCoroutineStub
) = withContext(Dispatchers.IO) {
    val starsystem = call.request.queryParameters["starsystem"] ?: "default-starsystem"
    val asteroid = call.request.queryParameters["asteroid"] ?: "default-asteroid"
    
    // Log HTTP request details
    logger.info("Request parameters: starsystem=$starsystem, asteroid=$asteroid")
    logger.info("Request headers: ${call.request.headers.entries().joinToString(", ") { "${it.key}=${it.value}" }}")

    try {
        val galaxyA = async {
            try {
                val request = GetGalaxiesRequest.newBuilder()
                    .setStarsystem(starsystem)
                    .setAsteroid(asteroid)
                    .build()
                // Use Kotlin coroutine stub
                val response = galaxyStub.getGalaxies(request)
                GalaxyDto(
                    id = response.getResults(0).getId(),
                    name = response.getResults(0).getName()
                )
            } catch (e: Exception) {
                logger.error("Error getting galaxy data", e)
                GalaxyDto(id = "error", name = "Galaxy service unavailable")
            }
        }

        val nebulaA = async {
            try {
                val request = CosmosRequest.newBuilder()
                    .setStarsystem(starsystem)
                    .setAsteroid(asteroid)
                    .build()
                // Use Kotlin coroutine stub
                val response = nebulaStub.getNebula(request)
                NebulaDto(dust = response.getDust())
            } catch (e: Exception) {
                logger.error("Error getting nebula data", e)
                NebulaDto(dust = "Nebula service unavailable")
            }
        }

        val quasarA = async {
            try {
                val request = CosmosRequest.newBuilder()
                    .setStarsystem(starsystem)
                    .setAsteroid(asteroid)
                    .build()
                // Use Kotlin coroutine stub
                val response = quasarStub.getQuasar(request)
                QuasarDto(energySignature = response.getEnergySignature())
            } catch (e: Exception) {
                logger.error("Error getting quasar data", e)
                QuasarDto(energySignature = "Quasar service unavailable")
            }
        }

        val wormholeA = async {
            try {
                val request = CosmosRequest.newBuilder()
                    .setStarsystem(starsystem)
                    .setAsteroid(asteroid)
                    .build()
                // Use Kotlin coroutine stub
                val response = wormholeStub.getWormhole(request)
                WormholeDto(destination = response.getDestination())
            } catch (e: Exception) {
                logger.error("Error getting wormhole data", e)
                WormholeDto(destination = "Wormhole service unavailable")
            }
        }

        val response = CosmosResponse(
            galaxy = galaxyA.await(),
            nebula = nebulaA.await(),
            quasar = quasarA.await(),
            wormhole = wormholeA.await()
        )

        call.respond(response)
    } catch (e: Exception) {
        logger.error("Error processing request", e)
        throw e
    }
}

// Remove the Java-style extension functions since we're now using the Kotlin stubs directly

@Serializable
data class GalaxyDto(val id: String, val name: String)
@Serializable
data class NebulaDto(val dust: String)
@Serializable
data class QuasarDto(val energySignature: String)
@Serializable
data class WormholeDto(val destination: String)

@Serializable
data class CosmosResponse(
    val galaxy: GalaxyDto,
    val nebula: NebulaDto,
    val quasar: QuasarDto,
    val wormhole: WormholeDto
)