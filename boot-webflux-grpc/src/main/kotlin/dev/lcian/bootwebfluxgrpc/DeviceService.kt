package dev.lcian.bootwebfluxgrpc

import io.grpc.stub.StreamObserver
import org.springframework.stereotype.Service
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

@Service
class DeviceService : DeviceServiceGrpc.DeviceServiceImplBase() {
    override fun getUploadUrl(request: UploadRequest, responseObserver: StreamObserver<GetUploadUrlResponse>) {
        // Simulate some CPU-intensive work
        val result = (1..1000000).fold(0.0) { acc, _ ->
            acc + Math.sin(Random.nextDouble())
        }
        
        // Simulate some IO delay (100-300ms)
        Thread.sleep(Random.nextLong(100, 300))
        
        val response = GetUploadUrlResponse.newBuilder()
            .setUploadUrl("https://example.com/upload/${request.deviceId}/${request.fileName}?computation=$result")
            .build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
} 