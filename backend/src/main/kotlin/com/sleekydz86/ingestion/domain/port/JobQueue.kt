package com.sleekydz86.ingestion.domain.port

import com.sleekydz86.ingestion.domain.model.JobId

interface JobQueue {
    suspend fun enqueue(jobId: com.sleekydz86.ingestion.domain.model.JobId)
    suspend fun dequeue(): com.sleekydz86.ingestion.domain.model.JobId?
    suspend fun size(): Int
}
