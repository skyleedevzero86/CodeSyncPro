package com.sleekydz86.ingestion.application.usecase

import com.sleekydz86.ingestion.domain.model.ItemStatus
import com.sleekydz86.ingestion.domain.model.Job
import com.sleekydz86.ingestion.domain.model.JobId
import com.sleekydz86.ingestion.domain.model.JobItem
import com.sleekydz86.ingestion.domain.model.JobProgress
import com.sleekydz86.ingestion.domain.model.JobStatus
import com.sleekydz86.ingestion.domain.port.JobRepository
import org.springframework.stereotype.Component


@Component
class GetJobStatusUseCase(
    private val jobRepository: JobRepository,
) {
    fun execute(jobId: JobId): JobStatusResponse {
        val job = jobRepository.findById(jobId)
            ?: throw JobNotFoundException("Job not found: ${jobId.value}")

        return JobStatusResponse(
            jobId = job.id.value,
            status = job.status,
            progress = job.progress,
            startedAt = job.startedAt,
            completedAt = job.completedAt,
            cancelledAt = job.cancelledAt,
            items = job.items.map { item: JobItem ->
                JobItemResponse(
                    itemId = item.id.value,
                    projectPath = item.projectPath,
                    filePath = item.filePath,
                    status = item.status,
                    error = item.error?.let { error ->
                        ErrorResponse(
                            code = error.code.name,
                            message = error.message,
                            retryable = error.retryable,
                        )
                    },
                    retryCount = item.retryCount,
                    nextRetryAt = item.nextRetryAt,
                    processedAt = item.processedAt,
                )
            },
        )
    }
}

data class JobStatusResponse(
    val jobId: String,
    val status: JobStatus,
    val progress: JobProgress,
    val startedAt: java.time.Instant?,
    val completedAt: java.time.Instant?,
    val cancelledAt: java.time.Instant?,
    val items: List<JobItemResponse>,
)

data class JobItemResponse(
    val itemId: String,
    val projectPath: String,
    val filePath: String,
    val status: ItemStatus,
    val error: ErrorResponse?,
    val retryCount: Int,
    val nextRetryAt: java.time.Instant?,
    val processedAt: java.time.Instant?,
)

data class ErrorResponse(
    val code: String,
    val message: String,
    val retryable: Boolean,
)

class JobNotFoundException(message: String) : RuntimeException(message)
