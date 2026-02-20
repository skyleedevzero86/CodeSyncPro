package com.sleekydz86.ingestion.domain.service

import com.sleekydz86.ingestion.domain.model.ErrorCode
import com.sleekydz86.ingestion.domain.model.ItemError
import java.time.Duration
import java.time.Instant
import kotlin.math.pow

/** Immutable retry configuration; behavior is implemented as pure functions. */
data class RetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelay: Duration = Duration.ofSeconds(1),
    val maxDelay: Duration = Duration.ofMinutes(5),
    val multiplier: Double = 2.0,
) {
    fun calculateNextRetryAt(retryCount: Int, baseTime: Instant = Instant.now()): Instant {
        check(retryCount < maxAttempts) { "Maximum retry attempts exceeded" }
        val delaySeconds = (initialDelay.seconds * multiplier.pow(retryCount)).toLong()
        val delay = Duration.ofSeconds(delaySeconds.coerceAtMost(maxDelay.seconds))
        return baseTime.plus(delay)
    }

    fun shouldRetry(error: ItemError, currentRetryCount: Int): Boolean =
        error.retryable &&
            currentRetryCount < maxAttempts &&
            error.code in RetryPolicyDefaults.retryableErrorCodes

    private object RetryPolicyDefaults {
        val retryableErrorCodes: Set<ErrorCode> = setOf(
            ErrorCode.TIMEOUT,
            ErrorCode.NETWORK_ERROR,
            ErrorCode.RATE_LIMIT_EXCEEDED,
            ErrorCode.EMBEDDING_API_ERROR,
        )
    }
}
