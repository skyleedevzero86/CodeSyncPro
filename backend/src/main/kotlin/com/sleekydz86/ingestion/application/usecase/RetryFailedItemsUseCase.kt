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
        val job: Job = jobRepository.findById(jobId)
            ?: throw JobNotFoundException("Job not found: ${jobId.value}")

        if (!job.hasFailedItems()) {
            throw NoFailedItemsException("Job has no failed items")
        }

        val failedItems = if (itemIds != null) {
            job.getFailedItems().filter { item: JobItem -> item.id.value in itemIds }
        } else {
            job.getFailedItems()
        }

        val itemsToRetry = failedItems.filter { item: JobItem ->
            item.canRetry() && retryPolicy.shouldRetry(
                item.error!!,
                item.retryCount,
            )
        }

        if (itemsToRetry.isEmpty()) {
            throw NoRetryableItemsException("No retryable items found")
        }

        val now = Instant.now()
        val updatedItems = itemsToRetry.map { item: JobItem ->
            val nextRetryAt = retryPolicy.calculateNextRetryAt(item.retryCount, now)
            item.markAsRetrying(nextRetryAt)
        }

        val updatedJob = job.copy(
            status = JobStatus.RETRYING,
            items = job.items.map { existingItem: JobItem ->
                updatedItems.find { updated: JobItem -> updated.id == existingItem.id } ?: existingItem
            },
        )

        jobRepository.update(updatedJob)
        jobQueue.enqueue(jobId)

        return RetryJobResponse(
            jobId = jobId.value,
            itemsToRetry = itemsToRetry.size,
        )
    }
}

data class RetryJobResponse(
    val jobId: String,
    val itemsToRetry: Int,
)

class NoFailedItemsException(message: String) : RuntimeException(message)
class NoRetryableItemsException(message: String) : RuntimeException(message)
