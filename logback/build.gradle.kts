plugins {
    kotlin("jvm") version "2.0.21"
}

group = "dev.lcian"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("ch.qos.logback:logback-core:1.5.17")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("io.sentry:sentry-logback:8.4.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}