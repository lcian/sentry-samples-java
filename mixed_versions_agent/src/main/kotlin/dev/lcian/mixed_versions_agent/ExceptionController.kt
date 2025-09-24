package dev.lcian.mixed_versions_agent

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exception")
class ExceptionController {

    @GetMapping
    fun throwException(): String {
        throw RuntimeException("Intentional Exception for Sentry + OTEL testing")
    }
}