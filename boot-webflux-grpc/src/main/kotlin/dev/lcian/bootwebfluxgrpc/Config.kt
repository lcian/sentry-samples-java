package dev.lcian.bootwebfluxgrpc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.grpc.Server
import io.grpc.ServerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter
import org.springframework.web.reactive.config.WebFluxConfigurer
import jakarta.annotation.PreDestroy

@Configuration
class Config : WebFluxConfigurer {
    private var grpcServer: Server? = null

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
        }
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val objectMapper = objectMapper()
        configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
    }

    @Bean
    fun grpcServer(deviceService: DeviceService): Server {
        return ServerBuilder.forPort(9090)
            .addService(deviceService)
            .build()
            .start()
            .also { grpcServer = it }
    }

    @PreDestroy
    fun stopGrpcServer() {
        grpcServer?.shutdown()
    }

    @Bean
    fun protobufHttpMessageConverter(): ProtobufHttpMessageConverter {
        return ProtobufHttpMessageConverter()
    }
} 