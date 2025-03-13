package dev.lcian.bootwebfluxgrpc

import io.grpc.stub.StreamObserver
import reactor.core.publisher.MonoSink

object MonoUtil {
    @JvmStatic
    fun <V> createStreamObserver(monoSink: MonoSink<V>): StreamObserver<V> {
        return object : StreamObserver<V> {
            override fun onNext(v: V) {
                monoSink.success(v)
            }

            override fun onError(throwable: Throwable) {
                monoSink.error(throwable)
            }

            override fun onCompleted() {
                // DO NOTHING
            }
        }
    }
} 