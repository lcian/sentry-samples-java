import com.google.protobuf.gradle.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    id("io.sentry.jvm.gradle") version "5.3.0"
    id("com.google.protobuf") version "0.9.4" // for gRPC
    kotlin("plugin.serialization") version "1.9.22" // match your Kotlin version
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc", 
                   "build/generated/source/proto/main/java",
                   "build/generated/source/proto/main/grpckt",   // Add Kotlin gRPC directory
                   "build/generated/source/proto/main/kotlin")   // Add Kotlin protobuf directory
        }
    }
}


group = "dev.lcian"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.9.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.13")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.13")
    implementation("io.ktor:ktor-server-status-pages:2.3.13")

    implementation("io.grpc:grpc-netty-shaded:1.67.1")
    implementation("io.grpc:grpc-protobuf:1.67.1")
    implementation("io.grpc:grpc-stub:1.67.1")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("com.google.protobuf:protobuf-java-util:3.25.6")
    implementation("com.google.protobuf:protobuf-kotlin:3.25.1") // Add Kotlin protobuf support

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.63.0"
        }
        // Add Kotlin gRPC plugin
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                // Add Kotlin gRPC plugin to tasks
                id("grpckt")
            }
            // Add Kotlin protobuf generation
            it.builtins {
                id("kotlin")
            }
        }
    }
}