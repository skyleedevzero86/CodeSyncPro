package com.sleekydz86.ingestion.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobProcessorConfig {

    @Bean
    fun maxConcurrency(@Value("\${ingestion.job.queue.maxConcurrency:5}") maxConcurrency: Int): Int {
        return maxConcurrency
    }
}
