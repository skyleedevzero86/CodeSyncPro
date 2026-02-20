package com.sleekydz86.ingestion.application.usecase

import com.sleekydz86.ingestion.domain.model.JobId
import com.sleekydz86.ingestion.domain.port.JobRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CancelJobUseCase(
    private val jobRepository: JobRepository,
) {
    fun execute(jobId: JobId) {
        val job = jobRepository.findById(jobId)
            ?: throw JobNotFoundException("Job not found: ${jobId.value}")
        if (!job.canBeCancelled()) throw JobCannotBeCancelledException("Job cannot be cancelled in status: ${job.status}")
        jobRepository.update(job.markAsCancelled(Instant.now()))
    }
}

class JobCannotBeCancelledException(message: String) : RuntimeException(message)
