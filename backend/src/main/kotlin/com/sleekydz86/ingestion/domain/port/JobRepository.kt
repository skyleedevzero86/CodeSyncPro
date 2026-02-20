package com.sleekydz86.ingestion.domain.port

import java.time.Instant

interface JobRepository {
    fun save(job: com.sleekydz86.ingestion.domain.model.Job): com.sleekydz86.ingestion.domain.model.Job
    fun findById(jobId: com.sleekydz86.ingestion.domain.model.JobId): com.sleekydz86.ingestion.domain.model.Job?
    fun findByStatus(status: com.sleekydz86.ingestion.domain.model.JobStatus, limit: Int, offset: Int): List<com.sleekydz86.ingestion.domain.model.Job>
    fun findAll(limit: Int, offset: Int): List<com.sleekydz86.ingestion.domain.model.Job>
    fun findByCreatedAtBetween(from: Instant, to: Instant, limit: Int, offset: Int): List<com.sleekydz86.ingestion.domain.model.Job>
    fun update(job: com.sleekydz86.ingestion.domain.model.Job): com.sleekydz86.ingestion.domain.model.Job
    fun delete(jobId: com.sleekydz86.ingestion.domain.model.JobId)
}
