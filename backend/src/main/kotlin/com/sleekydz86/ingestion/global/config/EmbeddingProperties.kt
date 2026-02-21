package com.sleekydz86.ingestion.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ingestion.embedding")
data class EmbeddingProperties(
    val endpointUrl: String = "http://localhost:8000/upsert",
    val apiKey: String = "",
    val timeoutSeconds: Int = 30,
)
