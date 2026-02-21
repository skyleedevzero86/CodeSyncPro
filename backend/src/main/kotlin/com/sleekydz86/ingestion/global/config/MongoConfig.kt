package com.sleekydz86.ingestion.global.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class MongoConfig(
    private val environment: Environment,
) {

    @Bean
    fun mongoClient(): MongoClient {
        var uri = environment.getProperty("spring.data.mongodb.uri") ?: ""
        if (uri.isBlank() || !uri.contains("@")) {
            uri = DEFAULT_MONGO_URI
            log.warn("MongoDB URI가 비어 있거나 인증 정보가 없어 기본(인증) URI를 사용합니다.")
        }
        log.info("MongoDB URI (마스킹): {}", uri.replace(Regex("://([^:]+):([^@]+)@"), "://$1:***@"))
        return MongoClients.create(uri)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MongoConfig::class.java)
        private const val DEFAULT_MONGO_URI =
            "mongodb://admin:admin123@localhost:27017/ingestion_service?authSource=admin"
    }
}
