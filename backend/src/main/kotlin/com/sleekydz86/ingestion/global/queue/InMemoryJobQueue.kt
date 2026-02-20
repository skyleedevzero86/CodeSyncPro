package com.sleekydz86.ingestion.global.queue


import com.sleekydz86.ingestion.domain.model.JobId
import com.sleekydz86.ingestion.domain.port.JobQueue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Component
@Component
class InMemoryJobQueue : JobQueue {
    private val queue = Channel<JobId>(Channel.UNLIMITED)
    private val mutex = Mutex()
    private var size = 0

    override suspend fun enqueue(jobId: JobId) {
        mutex.withLock {
            queue.send(jobId)
            size++
        }
    }

    override suspend fun dequeue(): JobId? {
        return mutex.withLock {
            val jobId = queue.tryReceive().getOrNull()
            if (jobId != null) {
                size--
            }
            jobId
        }
    }

    override suspend fun size(): Int {
        return mutex.withLock { size }
    }
}
