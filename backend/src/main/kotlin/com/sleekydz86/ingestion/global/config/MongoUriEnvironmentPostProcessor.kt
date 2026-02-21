package com.sleekydz86.ingestion.global.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

class MongoUriEnvironmentPostProcessor : EnvironmentPostProcessor {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val fromEnv = environment.getProperty("MONGODB_URI") ?: return
        if (fromEnv.isBlank()) return
        val source = MapPropertySource(
            "mongoUriOverride",
            mapOf("spring.data.mongodb.uri" to fromEnv)
        )
        environment.propertySources.addFirst(source)
    }
}
