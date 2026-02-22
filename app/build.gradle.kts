plugins {
	id("oris.spring-boot-app")
}

group = "com.kaanf"
version = "0.0.1-SNAPSHOT"
description = "Oris Backend"

dependencies {
	implementation(projects.chat)
	implementation(projects.user)
	implementation(projects.notification)
	implementation(projects.common)

	implementation(libs.kotlin.reflect)
	implementation(libs.spring.boot.starter.security)

	implementation(libs.spring.boot.starter.mail)
	implementation(libs.spring.boot.starter.amqp)
	implementation(libs.spring.boot.starter.data.redis)
	implementation(libs.spring.boot.starter.data.jpa)
	runtimeOnly(libs.postgresql)
	testRuntimeOnly("com.h2database:h2")
}
