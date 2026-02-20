package com.sleekydz86.ingestion.global.config


import com.sleekydz86.ingestion.domain.service.RetryPolicy
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class RetryPolicyConfig {

    @Bean
    fun retryPolicy(
        @ConfigurationProperties(prefix = "ingestion.job.retry") config: RetryPolicyProperties,
    ): RetryPolicy {
        return RetryPolicy(
            maxAttempts = config.maxAttempts,
            initialDelay = config.initialDelay,
            maxDelay = config.maxDelay,
            multiplier = config.multiplier,
        )
    }
}

@ConfigurationProperties(prefix = "ingestion.job.retry")
data class RetryPolicyProperties(
    val maxAttempts: Int = 3,
    val initialDelay: Duration = Duration.ofSeconds(1),
    val maxDelay: Duration = Duration.ofMinutes(5),
    val multiplier: Double = 2.0,
)
