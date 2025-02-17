import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding

plugins {
    kotlin("jvm") version "2.0.21"
    id("io.sentry.jvm.gradle") version "5.2.0"
    id("com.diffplug.spotless") version "7.0.2"
    application
}

kotlin { jvmToolchain(17) }

group = "dev.lcian"
version = "1.0-SNAPSHOT"

application { mainClass.set("dev.lcian.MainKt") }

repositories { mavenCentral() }

dependencies {
    implementation("software.amazon.awssdk:dynamodb:2.20.0")
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }

tasks {
    val fatJar =
        register<Jar>("fatJar") {
            dependsOn.addAll(
                listOf(
                    "compileJava",
                    "compileKotlin",
                    "processResources",
                ),
            )
            archiveClassifier.set("standalone")
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
            val sourcesMain = sourceSets.main.get()
            val contents =
                configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } +
                    sourcesMain.output
            from(contents)
        }
    build { dependsOn(fatJar) }
}

configure<SpotlessExtension> {
    lineEndings = LineEnding.UNIX
    kotlin {
        ktlint()
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        ktlint()
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
}
