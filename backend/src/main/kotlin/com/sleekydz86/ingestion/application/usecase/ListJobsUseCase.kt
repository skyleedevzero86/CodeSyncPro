package com.sleekydz86.ingestion.application.usecase

import com.sleekydz86.ingestion.domain.model.JobItem
import com.sleekydz86.ingestion.domain.port.JobRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class ListJobsUseCase(
    private val jobRepository: JobRepository,
) {
    fun execute(limit: Int = 100, offset: Int = 0): List<JobStatusResponse> {
        val to = Instant.now()
        val from = to.minus(365, ChronoUnit.DAYS)
        val jobs = jobRepository.findByCreatedAtBetween(from, to, limit.coerceIn(1, 500), offset)
        return jobs.map { job ->
            JobStatusResponse(
                jobId = job.id.value,
                status = job.status,
                progress = job.progress,
                startedAt = job.startedAt,
                completedAt = job.completedAt,
                cancelledAt = job.cancelledAt,
                items = job.items.map { it.toItemResponse() },
            )
        }
    }
}

private fun JobItem.toItemResponse() = JobItemResponse(
    itemId = id.value,
    projectPath = projectPath,
    filePath = filePath,
    status = status,
    error = error?.let { ErrorResponse(it.code.name, it.message, it.retryable) },
    retryCount = retryCount,
    nextRetryAt = nextRetryAt,
    processedAt = processedAt,
)
