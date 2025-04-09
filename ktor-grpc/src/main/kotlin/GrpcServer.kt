package dev.lcian

import io.grpc.BindableService
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import config.GalaxyServiceGrpc
import config.NebulaServiceGrpc
import config.QuasarServiceGrpc
import config.WormholeServiceGrpc
import config.Config.GetGalaxiesRequest
import config.Config.GalaxiesResponse
import config.Config.Galaxy
import config.Config.CosmosRequest
import config.Config.Nebula
import config.Config.Quasar
import config.Config.Wormhole
import kotlin.random.Random
// Add Kotlin gRPC imports
import config.GalaxyServiceGrpcKt
import config.NebulaServiceGrpcKt
import config.QuasarServiceGrpcKt
import config.WormholeServiceGrpcKt

// Base gRPC server class
abstract class BaseGrpcServer(private val port: Int) {
    private val server = ServerBuilder
        .forPort(port)
        .addService(createService())
        .build()
        
    // Abstract method that derived classes will implement to provide the appropriate service
    abstract fun createService(): BindableService
    
    fun start() {
        server.start()
        println("gRPC Server started on port $port for ${this.javaClass.simpleName}")
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Shutting down gRPC server on port $port")
            server.shutdown()
        })
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}

// Specific server for Galaxy service
class GalaxyGrpcServer : BaseGrpcServer(9091) {
    override fun createService(): BindableService = GalaxyServiceImpl()
}

// Specific server for Nebula service
class NebulaGrpcServer : BaseGrpcServer(9092) {
    override fun createService(): BindableService = NebulaServiceImpl()
}

// Specific server for Quasar service
class QuasarGrpcServer : BaseGrpcServer(9093) {
    override fun createService(): BindableService = QuasarServiceImpl()
}

// Specific server for Wormhole service
class WormholeGrpcServer : BaseGrpcServer(9094) {
    override fun createService(): BindableService = WormholeServiceImpl()
}

// Using Kotlin gRPC service implementation
class GalaxyServiceImpl : GalaxyServiceGrpcKt.GalaxyServiceCoroutineImplBase() {
    override suspend fun getGalaxies(request: GetGalaxiesRequest): GalaxiesResponse {
        // Simulate network delay
        delay(Random.nextLong(0, 100))
        
        val galaxy = Galaxy.newBuilder()
            .setId(request.getStarsystem())
            .setName(request.getAsteroid())
            .build()
            
        return GalaxiesResponse.newBuilder()
            .addResults(galaxy)
            .build()
    }
}

// Using Kotlin gRPC service implementation
class NebulaServiceImpl : NebulaServiceGrpcKt.NebulaServiceCoroutineImplBase() {
    override suspend fun getNebula(request: CosmosRequest): Nebula {
        // Simulate network delay
        delay(Random.nextLong(0, 100))
        
        return Nebula.newBuilder()
            .setDust(request.getAsteroid())
            .build()
    }
}

// Using Kotlin gRPC service implementation
class QuasarServiceImpl : QuasarServiceGrpcKt.QuasarServiceCoroutineImplBase() {
    override suspend fun getQuasar(request: CosmosRequest): Quasar {
        // Simulate network delay
        delay(Random.nextLong(0, 100))
        
        return Quasar.newBuilder()
            .setEnergySignature(request.getAsteroid())
            .build()
    }
}

// Using Kotlin gRPC service implementation
class WormholeServiceImpl : WormholeServiceGrpcKt.WormholeServiceCoroutineImplBase() {
    override suspend fun getWormhole(request: CosmosRequest): Wormhole {
        // Simulate network delay
        delay(Random.nextLong(0, 100))
        
        return Wormhole.newBuilder()
            .setDestination(request.getAsteroid())
            .build()
    }
} 