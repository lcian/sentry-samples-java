package dev.lcian.mixed_versions_agent

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MixedVersionsAgentApplication

fun main(args: Array<String>) {
	runApplication<MixedVersionsAgentApplication>(*args)
}
