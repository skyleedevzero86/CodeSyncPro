package com.sleekydz86.ingestion.domain.model


import java.time.Instant

data class JobItem(
    val id: ItemId,
    val jobId: JobId,
    val projectPath: String,
    val filePath: String,
    val status: ItemStatus,
    val error: ItemError? = null,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val nextRetryAt: Instant? = null,
    val processedAt: Instant? = null,
    val metadata: Map<String, String> = emptyMap(),
) {
    fun canRetry(): Boolean {
        return status == ItemStatus.FAILED &&
                error?.retryable == true &&
                retryCount < maxRetries
    }

    fun markAsRetrying(nextRetryAt: Instant): JobItem {
        return copy(
            status = ItemStatus.RETRYING,
            retryCount = retryCount + 1,
            nextRetryAt = nextRetryAt,
        )
    }

    fun markAsSuccess(processedAt: Instant): JobItem {
        return copy(
            status = ItemStatus.SUCCESS,
            processedAt = processedAt,
        )
    }

    fun markAsFailed(error: ItemError): JobItem {
        return copy(
            status = ItemStatus.FAILED,
            error = error,
        )
    }

    fun markAsSkipped(reason: String): JobItem {
        return copy(
            status = ItemStatus.SKIPPED,
            metadata = metadata + ("skipReason" to reason),
        )
    }
}

@JvmInline
value class ItemId(val value: String)

enum class ItemStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    RETRYING,
    SKIPPED,
}

data class ItemError(
    val code: ErrorCode,
    val message: String,
    val retryable: Boolean,
    val occurredAt: Instant = Instant.now(),
)

enum class ErrorCode {
    TIMEOUT,
    NETWORK_ERROR,
    AUTHENTICATION_ERROR,
    RATE_LIMIT_EXCEEDED,
    FILE_TOO_LARGE,
    BINARY_FILE,
    INVALID_FORMAT,
    EMBEDDING_API_ERROR,
    GIT_ERROR,
    UNKNOWN_ERROR,
}
