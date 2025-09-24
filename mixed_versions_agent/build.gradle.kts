plugins {
	kotlin("jvm") version libs.versions.kotlin
	kotlin("plugin.spring") version libs.versions.kotlin
	id("org.springframework.boot") version libs.versions.spring.boot
	id("io.spring.dependency-management") version libs.versions.spring.dependency.management
    id("io.sentry.jvm.gradle") version libs.versions.sentry.gradle
}

group = "dev.lcian"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInt())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(libs.spring.boot.starter.web)
	implementation(libs.jackson.module.kotlin)
	implementation(libs.kotlin.reflect)

	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.kotlin.test.junit5)
	testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

sentry {
	debug.set(true)
	includeSourceContext.set(false)
	includeDependenciesReport.set(false)
	autoInstallation {
		enabled.set(true)
	}
}
