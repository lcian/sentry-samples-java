package dev.lcian.springbootwar

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/test")
    fun test(): String {
        return "Hello from Spring Boot WAR application!"
    }
} 