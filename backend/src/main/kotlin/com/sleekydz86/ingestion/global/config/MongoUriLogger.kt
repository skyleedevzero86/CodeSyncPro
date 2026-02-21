package com.sleekydz86.ingestion.global.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MongoUriLogger(
    @Value("\${spring.data.mongodb.uri:}") private val uri: String,
) {

    @PostConstruct
    fun logUri() {
        val masked = uri.replace(Regex("://([^:]+):([^@]+)@"), "://$1:***@")
        log.info("MongoDB URI (masked): {}", if (uri.isBlank()) "(not set)" else masked)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MongoUriLogger::class.java)
    }
}
