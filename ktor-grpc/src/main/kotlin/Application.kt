package dev.lcian

import io.ktor.server.application.*
import io.sentry.Sentry
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("dev.lcian.Application")

fun main(args: Array<String>) {
    Sentry.init { options ->
        options.dsn = "https://b9ca97be3ff8f1cef41dffdcb1e5100b@o447951.ingest.us.sentry.io/4508683222843393"
        options.isDebug = true
        options.isSendDefaultPii = true
        options.tracesSampleRate = 1.0
    }
    
    // Start all gRPC servers in separate coroutines and wait for them to be ready
    runBlocking {
        val galaxyServer = GalaxyGrpcServer()
        val nebulaServer = NebulaGrpcServer()
        val quasarServer = QuasarGrpcServer()
        val wormholeServer = WormholeGrpcServer()
        
        val servers = listOf(
            async(Dispatchers.IO) { 
                logger.info("Starting Galaxy gRPC server on port 9091")
                galaxyServer.start() 
                true
            },
            async(Dispatchers.IO) { 
                logger.info("Starting Nebula gRPC server on port 9092")
                nebulaServer.start() 
                true
            },
            async(Dispatchers.IO) { 
                logger.info("Starting Quasar gRPC server on port 9093")
                quasarServer.start() 
                true
            },
            async(Dispatchers.IO) { 
                logger.info("Starting Wormhole gRPC server on port 9094")
                wormholeServer.start() 
                true
            }
        )
        
        // Wait for all servers to start
        servers.awaitAll()
        
        // Add a small delay to ensure servers are fully initialized
        delay(1000)
        logger.info("All gRPC servers started successfully")
    }
    
    // Now start the Ktor server
    logger.info("Starting Ktor server")
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    configureRouting()
}
