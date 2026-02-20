package com.sleekydz86.ingestion.domain.port


interface JobQueue {
    suspend fun enqueue(jobId: JobId)
    suspend fun dequeue(): JobId?
    suspend fun size(): Int
}
