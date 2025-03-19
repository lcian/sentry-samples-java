package dev.lcian.logback

import io.sentry.Sentry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Main {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    fun main() {
        Sentry.init {
            it.dsn = "https://b9ca97be3ff8f1cef41dffdcb1e5100b@o447951.ingest.us.sentry.io/4508683222843393"
            it.isDebug = true
            it.tracesSampleRate = 1.0
        }
        a()
        b()
        c()
        d()
    }
    fun a() {
        logger.warn("Warning without exception");
    }
    fun b(x: Int = 6){
        if (x == 0) {
            logger.error("Error without exception");
        } else {
            b(x - 1)
        }
    }
    fun c() {
        val e = RuntimeException("something")
        logger.warn("Warning with exception", e)
    }
    fun d() {
        val e = IllegalArgumentException("something else")
        logger.error("Error with exception", e)
    }
}

fun main() {
    Main().main()
}
