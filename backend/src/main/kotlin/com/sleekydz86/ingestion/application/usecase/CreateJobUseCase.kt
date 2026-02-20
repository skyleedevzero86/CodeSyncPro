package com.sleekydz86.ingestion.application.usecase

import com.sleekydz86.ingestion.domain.model.Job
import com.sleekydz86.ingestion.domain.model.JobId
import com.sleekydz86.ingestion.domain.model.JobOptions
import com.sleekydz86.ingestion.domain.model.JobProgress
import com.sleekydz86.ingestion.domain.model.JobStatus
import com.sleekydz86.ingestion.domain.model.SourceConfig
import com.sleekydz86.ingestion.domain.model.SourceType
import com.sleekydz86.ingestion.domain.port.JobQueue
import com.sleekydz86.ingestion.domain.port.JobRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CreateJobUseCase(
    private val jobRepository: JobRepository,
    private val jobQueue: JobQueue,
) {
    suspend fun execute(request: CreateJobRequest): Job {
        val now = Instant.now()
        val job = Job(
            id = JobId.generate(),
            sourceType = request.sourceType,
            sourceConfig = request.sourceConfig,
            options = request.options,
            status = JobStatus.PENDING,
            progress = JobProgress(),
            createdAt = now,
            startedAt = null,
            completedAt = null,
            cancelledAt = null,
            callbackUrl = request.callbackUrl,
            items = emptyList(),
            metadata = emptyMap(),
        )
        return jobRepository.save(job).also { jobQueue.enqueue(job.id) }
    }
}

data class CreateJobRequest(
    val sourceType: SourceType,
    val sourceConfig: SourceConfig,
    val options: JobOptions,
    val callbackUrl: String? = null,
)
