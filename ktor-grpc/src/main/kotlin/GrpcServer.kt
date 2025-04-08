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

class GalaxyServiceImpl : GalaxyServiceGrpc.GalaxyServiceImplBase() {
    override fun getGalaxies(
        request: GetGalaxiesRequest,
        responseObserver: StreamObserver<GalaxiesResponse>
    ) {
        runBlocking {
            // Simulate network delay
            delay(Random.nextLong(0, 100))
            
            val galaxy = Galaxy.newBuilder()
                .setId(request.getStarsystem())
                .setName(request.getAsteroid())
                .build()
                
            val response = GalaxiesResponse.newBuilder()
                .addResults(galaxy)
                .build()
                
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }
    }
}

class NebulaServiceImpl : NebulaServiceGrpc.NebulaServiceImplBase() {
    override fun getNebula(
        request: CosmosRequest,
        responseObserver: StreamObserver<Nebula>
    ) {
        runBlocking {
            // Simulate network delay
            delay(Random.nextLong(0, 100))
            
            val response = Nebula.newBuilder()
                .setDust(request.getAsteroid())
                .build()
                
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }
    }
}

class QuasarServiceImpl : QuasarServiceGrpc.QuasarServiceImplBase() {
    override fun getQuasar(
        request: CosmosRequest,
        responseObserver: StreamObserver<Quasar>
    ) {
        runBlocking {
            // Simulate network delay
            delay(Random.nextLong(0, 100))
            
            val response = Quasar.newBuilder()
                .setEnergySignature(request.getAsteroid())
                .build()
                
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }
    }
}

class WormholeServiceImpl : WormholeServiceGrpc.WormholeServiceImplBase() {
    override fun getWormhole(
        request: CosmosRequest,
        responseObserver: StreamObserver<Wormhole>
    ) {
        runBlocking {
            // Simulate network delay
            delay(Random.nextLong(0, 100))
            
            val response = Wormhole.newBuilder()
                .setDestination(request.getAsteroid())
                .build()
                
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }
    }
} 