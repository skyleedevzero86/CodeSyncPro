package com.sleekydz86.ingestion.application.usecase

import com.sleekydz86.ingestion.domain.model.Job
import com.sleekydz86.ingestion.domain.model.JobItem
import com.sleekydz86.ingestion.domain.model.JobId
import com.sleekydz86.ingestion.domain.model.JobStatus
import com.sleekydz86.ingestion.domain.port.JobQueue
import com.sleekydz86.ingestion.domain.port.JobRepository
import com.sleekydz86.ingestion.domain.service.RetryPolicy
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RetryFailedItemsUseCase(
    private val jobRepository: JobRepository,
    private val jobQueue: JobQueue,
    private val retryPolicy: RetryPolicy,
) {
    suspend fun execute(jobId: JobId, itemIds: List<String>? = null): RetryJobResponse {
        val job = jobRepository.findById(jobId)
            ?: throw JobNotFoundException("Job not found: ${jobId.value}")
        check(job.hasFailedItems()) { throw NoFailedItemsException("Job has no failed items") }

        val failedItems = job.getFailedItems()
            .let { if (itemIds != null) it.filter { item -> item.id.value in itemIds } else it }
        val itemsToRetry = failedItems.filter { item ->
            item.canRetry() && item.error != null && retryPolicy.shouldRetry(item.error!!, item.retryCount)
        }
        check(itemsToRetry.isNotEmpty()) { throw NoRetryableItemsException("No retryable items found") }

        val now = Instant.now()
        val updatedItemsById = itemsToRetry.associate { it.id to it.markAsRetrying(retryPolicy.calculateNextRetryAt(it.retryCount, now)) }
        val updatedJob = job.copy(
            status = JobStatus.RETRYING,
            items = job.items.map { existing -> updatedItemsById[existing.id] ?: existing },
        )

        jobRepository.update(updatedJob)
        jobQueue.enqueue(jobId)
        return RetryJobResponse(jobId = jobId.value, itemsToRetry = itemsToRetry.size)
    }
}

data class RetryJobResponse(
    val jobId: String,
    val itemsToRetry: Int,
)

class NoFailedItemsException(message: String) : RuntimeException(message)
class NoRetryableItemsException(message: String) : RuntimeException(message)
