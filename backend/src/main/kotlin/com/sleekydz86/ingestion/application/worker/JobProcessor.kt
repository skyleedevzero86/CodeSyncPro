package com.sleekydz86.ingestion.application.worker


import com.sleekydz86.ingestion.application.usecase.ProcessJobUseCase
import com.sleekydz86.ingestion.domain.model.JobId
import com.sleekydz86.ingestion.domain.port.JobQueue
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withPermit
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class JobProcessor(
    private val jobQueue: JobQueue,
    private val processJobUseCase: ProcessJobUseCase,
    private val maxConcurrency: Int = 5,
) : ApplicationRunner {

    private val logger = Logger.getLogger(JobProcessor::class.java.name)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val semaphore = kotlinx.coroutines.sync.Semaphore(maxConcurrency)

        override fun run(args: ApplicationArguments) {
        logger.info("Job processor started")

        repeat(maxConcurrency) {
            scope.launch {
                processJobs()
            }
        }
    }

    private suspend fun processJobs() {
        while (true) {
            try {
                val jobId: JobId? = jobQueue.dequeue()
                if (jobId != null) {
                    semaphore.withPermit {
                        processJob(jobId)
                    }
                } else {
                    delay(1000)
                }
            } catch (e: Exception) {
                logger.severe("Error processing job: ${e.message}")
                delay(5000)
            }
        }
    }

    private suspend fun processJob(jobId: JobId) {
        try {
            logger.info("Processing job: ${jobId.value}")
            processJobUseCase.execute(jobId)
            logger.info("Job completed: ${jobId.value}")
        } catch (e: Exception) {
            logger.severe("Failed to process job ${jobId.value}: ${e.message}")
            throw e
        }
    }
}
