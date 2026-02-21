package com.sleekydz86.ingestion.global.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Int.MIN_VALUE)
class MongoUriLogger(
    @Value("\${spring.data.mongodb.uri:}") private val uri: String,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val masked = uri.replace(Regex("://([^:]+):([^@]+)@"), "://$1:***@")
        log.info("MongoDB URI (masked): {}", if (uri.isBlank()) "(not set)" else masked)
    }

    companion object {
        private val log = LoggerFactory.getLogger(MongoUriLogger::class.java)
    }
}
