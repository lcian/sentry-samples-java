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

private val logger = LoggerFactory.getLogger("dev.lcian.Routing")

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
    
    // Create the gRPC channels for each service
    val galaxyChannel = ManagedChannelBuilder.forAddress("localhost", 9091)
        .usePlaintext()
        .build()
    
    val nebulaChannel = ManagedChannelBuilder.forAddress("localhost", 9092)
        .usePlaintext()
        .build()
    
    val quasarChannel = ManagedChannelBuilder.forAddress("localhost", 9093)
        .usePlaintext()
        .build()
    
    val wormholeChannel = ManagedChannelBuilder.forAddress("localhost", 9094)
        .usePlaintext()
        .build()

    // Initialize the stubs
    val galaxyStub = GalaxyServiceGrpc.newStub(galaxyChannel)
    val nebulaStub = NebulaServiceGrpc.newStub(nebulaChannel)
    val quasarStub = QuasarServiceGrpc.newStub(quasarChannel)
    val wormholeStub = WormholeServiceGrpc.newStub(wormholeChannel)

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
    galaxyStub: GalaxyServiceGrpc.GalaxyServiceStub,
    nebulaStub: NebulaServiceGrpc.NebulaServiceStub,
    quasarStub: QuasarServiceGrpc.QuasarServiceStub,
    wormholeStub: WormholeServiceGrpc.WormholeServiceStub
) = withContext(Dispatchers.IO) {
    val starsystem = call.request.queryParameters["starsystem"] ?: "default-starsystem"
    val asteroid = call.request.queryParameters["asteroid"] ?: "default-asteroid"

    try {
        val galaxyA = async {
            try {
                val request = GetGalaxiesRequest.newBuilder()
                    .setStarsystem(starsystem)
                    .setAsteroid(asteroid)
                    .build()
                val response = galaxyStub.getGalaxiesAsync(request)
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
                val response = nebulaStub.getNebulaAsync(request)
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
                val response = quasarStub.getQuasarAsync(request)
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
                val response = wormholeStub.getWormholeAsync(request)
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

// Extension functions to convert gRPC async callbacks to coroutines
suspend fun GalaxyServiceGrpc.GalaxyServiceStub.getGalaxiesAsync(request: GetGalaxiesRequest): GalaxiesResponse = 
    suspendCoroutine { continuation ->
        getGalaxies(request, object : StreamObserver<GalaxiesResponse> {
            override fun onNext(response: GalaxiesResponse) {
                continuation.resume(response)
            }
            
            override fun onError(t: Throwable) {
                continuation.resumeWithException(t)
            }
            
            override fun onCompleted() {
                // Nothing to do here
            }
        })
    }

suspend fun NebulaServiceGrpc.NebulaServiceStub.getNebulaAsync(request: CosmosRequest): Nebula = 
    suspendCoroutine { continuation ->
        getNebula(request, object : StreamObserver<Nebula> {
            override fun onNext(response: Nebula) {
                continuation.resume(response)
            }
            
            override fun onError(t: Throwable) {
                continuation.resumeWithException(t)
            }
            
            override fun onCompleted() {
                // Nothing to do here
            }
        })
    }

suspend fun QuasarServiceGrpc.QuasarServiceStub.getQuasarAsync(request: CosmosRequest): Quasar = 
    suspendCoroutine { continuation ->
        getQuasar(request, object : StreamObserver<Quasar> {
            override fun onNext(response: Quasar) {
                continuation.resume(response)
            }
            
            override fun onError(t: Throwable) {
                continuation.resumeWithException(t)
            }
            
            override fun onCompleted() {
                // Nothing to do here
            }
        })
    }

suspend fun WormholeServiceGrpc.WormholeServiceStub.getWormholeAsync(request: CosmosRequest): Wormhole = 
    suspendCoroutine { continuation ->
        getWormhole(request, object : StreamObserver<Wormhole> {
            override fun onNext(response: Wormhole) {
                continuation.resume(response)
            }
            
            override fun onError(t: Throwable) {
                continuation.resumeWithException(t)
            }
            
            override fun onCompleted() {
                // Nothing to do here
            }
        })
    }

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