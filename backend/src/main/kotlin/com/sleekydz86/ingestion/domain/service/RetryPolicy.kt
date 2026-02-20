package com.sleekydz86.ingestion.domain.service

import com.sleekydz86.ingestion.domain.model.ItemError
import java.time.Duration
import java.time.Instant
import kotlin.math.pow

class RetryPolicy(
    private val maxAttempts: Int = 3,
    private val initialDelay: Duration = Duration.ofSeconds(1),
    private val maxDelay: Duration = Duration.ofMinutes(5),
    private val multiplier: Double = 2.0,
) {
    fun calculateNextRetryAt(retryCount: Int, baseTime: Instant = Instant.now()): Instant {
        if (retryCount >= maxAttempts) {
            throw IllegalArgumentException("Maximum retry attempts exceeded")
        }

        val delaySeconds = (initialDelay.seconds * multiplier.pow(retryCount)).toLong()
        val delay = Duration.ofSeconds(delaySeconds.coerceAtMost(maxDelay.seconds))

        return baseTime.plus(delay)
    }

    fun shouldRetry(error: ItemError, currentRetryCount: Int): Boolean {
        return error.retryable &&
                currentRetryCount < maxAttempts &&
                error.code in retryableErrorCodes
    }

    companion object {
        private val retryableErrorCodes = setOf(
            ItemError.ErrorCode.TIMEOUT,
            ItemError.ErrorCode.NETWORK_ERROR,
            ItemError.ErrorCode.RATE_LIMIT_EXCEEDED,
            ItemError.ErrorCode.EMBEDDING_API_ERROR,
        )
    }
}
