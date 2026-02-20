package com.sleekydz86.ingestion.application.usecase

import com.sleekydz86.ingestion.domain.model.Job
import com.sleekydz86.ingestion.domain.model.JobItem
import com.sleekydz86.ingestion.domain.model.JobStatus
import com.sleekydz86.ingestion.domain.port.JobRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class GetStatisticsUseCase(
    private val jobRepository: JobRepository,
) {
    fun execute(from: Instant, to: Instant, limit: Int = 10_000): StatisticsResponse {
        val jobs = jobRepository.findByCreatedAtBetween(from, to, limit, 0)
        val countByStatus = jobs.groupingBy { it.status }.eachCount()
        val statusCounts = JobStatus.entries.associate { it.name to (countByStatus[it] ?: 0) }
        val byDay = jobs
            .groupingBy { it.createdAt.atZone(ZoneOffset.UTC).toLocalDate() }
            .eachCount()
            .toSortedMap()
            .map { (k, v) -> DayCount(k.format(DateTimeFormatter.ISO_LOCAL_DATE), v) }

        val totalProcessedFiles = jobs.sumOf { it.progress.processedFiles }
        val totalFailedFiles = jobs.sumOf { it.progress.failedFiles }
        val totalSkippedFiles = jobs.sumOf { it.progress.skippedFiles }
        val totalProjects = jobs.sumOf { it.progress.totalProjects }

        val jobSummaries = jobs.map { job ->
            JobSummaryRow(
                jobId = job.id.value,
                status = job.status.name,
                sourceType = job.sourceType.name,
                mode = job.options.mode.name,
                createdAt = job.createdAt.toString(),
                startedAt = job.startedAt?.toString(),
                completedAt = job.completedAt?.toString(),
                totalProjects = job.progress.totalProjects,
                processedProjects = job.progress.processedProjects,
                totalFiles = job.progress.totalFiles,
                processedFiles = job.progress.processedFiles,
                failedFiles = job.progress.failedFiles,
                skippedFiles = job.progress.skippedFiles,
                itemCount = job.items.size,
            )
        }

        val errorCodeCounts = jobs
            .flatMap { it.items }
            .mapNotNull { it.error }
            .groupingBy { it.code.name }
            .eachCount()

        return StatisticsResponse(
            from = from.toString(),
            to = to.toString(),
            totalJobs = jobs.size,
            countByStatus = statusCounts,
            jobsByDay = byDay,
            totalProcessedFiles = totalProcessedFiles,
            totalFailedFiles = totalFailedFiles,
            totalSkippedFiles = totalSkippedFiles,
            totalProjects = totalProjects,
            jobSummaries = jobSummaries,
            errorCodeCounts = errorCodeCounts,
        )
    }
}

data class StatisticsResponse(
    val from: String,
    val to: String,
    val totalJobs: Int,
    val countByStatus: Map<String, Int>,
    val jobsByDay: List<DayCount>,
    val totalProcessedFiles: Int,
    val totalFailedFiles: Int,
    val totalSkippedFiles: Int,
    val totalProjects: Int,
    val jobSummaries: List<JobSummaryRow>,
    val errorCodeCounts: Map<String, Int>,
)

data class DayCount(val date: String, val count: Int)

data class JobSummaryRow(
    val jobId: String,
    val status: String,
    val sourceType: String,
    val mode: String,
    val createdAt: String,
    val startedAt: String?,
    val completedAt: String?,
    val totalProjects: Int,
    val processedProjects: Int,
    val totalFiles: Int,
    val processedFiles: Int,
    val failedFiles: Int,
    val skippedFiles: Int,
    val itemCount: Int,
)
