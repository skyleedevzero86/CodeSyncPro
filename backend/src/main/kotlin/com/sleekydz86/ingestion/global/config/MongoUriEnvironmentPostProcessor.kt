package com.sleekydz86.ingestion.global.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

class MongoUriEnvironmentPostProcessor : EnvironmentPostProcessor {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val fromEnv = environment.getProperty("MONGODB_URI")
        val uri = if (fromEnv.isNullOrBlank()) DEFAULT_MONGODB_URI else fromEnv
        val source = MapPropertySource(
            "mongoUriOverride",
            mapOf("spring.data.mongodb.uri" to uri)
        )
        environment.propertySources.addFirst(source)
    }

    companion object {
        private const val DEFAULT_MONGODB_URI =
            "mongodb://admin:admin123@localhost:27017/ingestion_service?authSource=admin"
    }
}
