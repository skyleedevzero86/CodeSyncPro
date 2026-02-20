package com.sleekydz86.ingestion.domain.port

import java.time.Instant

interface JobRepository {
    fun save(job: Job): Job
    fun findById(jobId: JobId): Job?
    fun findByStatus(status: JobStatus, limit: Int, offset: Int): List<Job>
    fun findAll(limit: Int, offset: Int): List<Job>
    fun findByCreatedAtBetween(from: Instant, to: Instant, limit: Int, offset: Int): List<Job>
    fun update(job: Job): Job
    fun delete(jobId: JobId)
}
