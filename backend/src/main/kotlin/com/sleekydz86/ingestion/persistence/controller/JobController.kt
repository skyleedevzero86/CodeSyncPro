package com.sleekydz86.ingestion.persistence.controller


import com.sleekydz86.ingestion.application.usecase.CancelJobUseCase
import com.sleekydz86.ingestion.application.usecase.CreateJobUseCase
import com.sleekydz86.ingestion.application.usecase.GetJobStatusUseCase
import com.sleekydz86.ingestion.application.usecase.JobStatusResponse
import com.sleekydz86.ingestion.application.usecase.RetryFailedItemsUseCase
import com.sleekydz86.ingestion.application.usecase.RetryJobResponse
import com.sleekydz86.ingestion.domain.model.Job
import com.sleekydz86.ingestion.domain.model.JobId
import com.sleekydz86.ingestion.domain.model.JobStatus
import com.sleekydz86.ingestion.persistence.dto.CreateJobRequest
import com.sleekydz86.ingestion.persistence.dto.CreateJobResponse
import com.sleekydz86.ingestion.persistence.dto.DtoMapper
import com.sleekydz86.ingestion.persistence.dto.RetryRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/jobs")
class JobController(
    private val createJobUseCase: CreateJobUseCase,
    private val getJobStatusUseCase: GetJobStatusUseCase,
    private val retryFailedItemsUseCase: RetryFailedItemsUseCase,
    private val cancelJobUseCase: CancelJobUseCase,
) {

    @PostMapping
    suspend fun createJob(@Valid @RequestBody request: CreateJobRequest): ResponseEntity<CreateJobResponse> =
        createJobUseCase.execute(DtoMapper.toUseCaseRequest(request)).let { job ->
            ResponseEntity.status(HttpStatus.ACCEPTED).body(
                CreateJobResponse(
                    jobId = job.id.value,
                    status = job.status,
                    createdAt = job.createdAt,
                    statusUrl = "/api/v1/jobs/${job.id.value}",
                )
            )
        }

    @GetMapping("/{jobId}")
    fun getJobStatus(@PathVariable jobId: String): ResponseEntity<JobStatusResponse> =
        ResponseEntity.ok(getJobStatusUseCase.execute(JobId(jobId)))

    @PostMapping("/{jobId}/retry")
    suspend fun retryFailedItems(
        @PathVariable jobId: String,
        @RequestBody request: RetryRequest,
    ): ResponseEntity<RetryJobResponse> =
        ResponseEntity.status(HttpStatus.ACCEPTED).body(
            retryFailedItemsUseCase.execute(JobId(jobId), request.itemIds)
        )

    @DeleteMapping("/{jobId}")
    fun cancelJob(@PathVariable jobId: String): ResponseEntity<Map<String, String>> {
        cancelJobUseCase.execute(JobId(jobId))
        return ResponseEntity.ok(mapOf("status" to "CANCELLED"))
    }
}
