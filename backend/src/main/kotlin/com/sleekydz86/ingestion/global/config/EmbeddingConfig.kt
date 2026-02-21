package com.sleekydz86.ingestion.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(EmbeddingProperties::class)
class EmbeddingConfig
