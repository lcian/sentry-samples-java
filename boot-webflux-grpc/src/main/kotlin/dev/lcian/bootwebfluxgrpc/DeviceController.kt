package dev.lcian.bootwebfluxgrpc

import io.grpc.ManagedChannelBuilder
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import jakarta.annotation.PreDestroy

@RestController
@RequestMapping("/device", produces = [MediaType.APPLICATION_JSON_VALUE])
class DeviceController {
    private val logger = LoggerFactory.getLogger(DeviceController::class.java)
    private val channel = ManagedChannelBuilder.forAddress("localhost", 9090)
        .usePlaintext()
        .build()
    private val stub = DeviceServiceGrpc.newStub(channel)

    @PostMapping(
        "/get_upload_url",
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getUploadUrl(@RequestBody request: RestUploadRequest): Mono<RestUploadUrlResponse> {
        logger.info("Received request: $request")
        return Mono.create<GetUploadUrlResponse> { emptyMonoSink: MonoSink<GetUploadUrlResponse> ->
            stub.getUploadUrl(
                request.toProto(),
                MonoUtil.createStreamObserver(emptyMonoSink)
            )
        }.map { response ->
            logger.info("Got response: $response")
            RestUploadUrlResponse.fromProto(response)
        }.doOnError { error ->
            logger.error("Error processing request", error)
        }
    }

    @PreDestroy
    fun shutdown() {
        channel.shutdown()
    }
}