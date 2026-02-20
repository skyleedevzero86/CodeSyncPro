package com.sleekydz86.ingestion.domain.model


import java.time.Instant
import java.util.UUID

data class Job(
    val id: JobId,
    val sourceType: SourceType,
    val sourceConfig: SourceConfig,
    val options: JobOptions,
    val status: JobStatus,
    val progress: JobProgress,
    val createdAt: Instant,
    val startedAt: Instant?,
    val completedAt: Instant?,
    val cancelledAt: Instant?,
    val callbackUrl: String?,
    val items: List<JobItem> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
) {
    fun canBeCancelled(): Boolean {
        return status in listOf(JobStatus.PENDING, JobStatus.PROCESSING)
    }

    fun isCompleted(): Boolean {
        return status in listOf(JobStatus.SUCCESS, JobStatus.PARTIAL_SUCCESS, JobStatus.FAILED, JobStatus.CANCELLED)
    }

    fun hasFailedItems(): Boolean {
        return items.any { it.status == ItemStatus.FAILED }
    }

    fun getFailedItems(): List<JobItem> {
        return items.filter { it.status == ItemStatus.FAILED }
    }

    fun updateProgress(processedProjects: Int, processedFiles: Int, failedFiles: Int): Job {
        return copy(
            progress = progress.copy(
                processedProjects = processedProjects,
                processedFiles = processedFiles,
                failedFiles = failedFiles,
            )
        )
    }

    fun markAsProcessing(startedAt: Instant): Job {
        return copy(
            status = JobStatus.PROCESSING,
            startedAt = startedAt,
        )
    }

    fun markAsSuccess(completedAt: Instant): Job {
        return copy(
            status = JobStatus.SUCCESS,
            completedAt = completedAt,
        )
    }

    fun markAsPartialSuccess(completedAt: Instant): Job {
        return copy(
            status = JobStatus.PARTIAL_SUCCESS,
            completedAt = completedAt,
        )
    }

    fun markAsFailed(completedAt: Instant, error: String): Job {
        return copy(
            status = JobStatus.FAILED,
            completedAt = completedAt,
            metadata = metadata + ("error" to error),
        )
    }

    fun markAsCancelled(cancelledAt: Instant): Job {
        return copy(
            status = JobStatus.CANCELLED,
            cancelledAt = cancelledAt,
        )
    }
}

@JvmInline
value class JobId(val value: String) {
    companion object {
        fun generate(): JobId = JobId(UUID.randomUUID().toString())
    }
}

enum class SourceType {
    GITLAB,
    GITHUB,
    BITBUCKET,
}

data class SourceConfig(
    val baseUrl: String,
    val accessToken: String,
    val projectIds: List<Long> = emptyList(),
    val groupIds: List<Long> = emptyList(),
    val targetBranch: String = "main",
    val shouldIncludeSubgroups: Boolean = true,
    val shouldIncludeArchived: Boolean = false,
    val shouldUseMembershipOnly: Boolean = true,
    val pageSize: Int = 100,
)

data class JobOptions(
    val mode: IngestMode,
    val fileFilters: FileFilters,
    val concurrency: ConcurrencyConfig,
    val cleanupAfterIngest: Boolean = true,
    val since: Instant? = null,
)

enum class IngestMode {
    FULL,
    INCREMENTAL,
}

data class FileFilters(
    val includeGlobs: List<String> = emptyList(),
    val excludeDirs: List<String> = emptyList(),
    val excludeFiles: List<String> = emptyList(),
    val maxFileSizeBytes: Long = 5_000_000,
    val skipBinary: Boolean = true,
)

data class ConcurrencyConfig(
    val projects: Int = 2,
    val files: Int = 8,
)

enum class JobStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    PARTIAL_SUCCESS,
    FAILED,
    CANCELLED,
    RETRYING,
}

data class JobProgress(
    val totalProjects: Int = 0,
    val processedProjects: Int = 0,
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val failedFiles: Int = 0,
    val skippedFiles: Int = 0,
) {
    fun getCompletionPercentage(): Double {
        if (totalFiles == 0) return 0.0
        return (processedFiles.toDouble() / totalFiles) * 100.0
    }
}
