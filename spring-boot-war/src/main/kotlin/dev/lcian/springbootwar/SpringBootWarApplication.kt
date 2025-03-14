package dev.lcian.springbootwar

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringBootWarApplication

fun main(args: Array<String>) {
    runApplication<SpringBootWarApplication>(*args)
}
