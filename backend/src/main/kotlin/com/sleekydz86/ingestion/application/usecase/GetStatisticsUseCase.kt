package com.sleekydz86.ingestion.application.usecase

import com.sleekydz86.ingestion.domain.model.JobStatus
import com.sleekydz86.ingestion.domain.port.JobRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.LinkedHashMap
import kotlin.collections.map

@Component
class GetStatisticsUseCase(
    private val jobRepository: JobRepository,
) {
    fun execute(from: Instant, to: Instant, limit: Int = 10_000): StatisticsResponse {
        val jobs = jobRepository.findByCreatedAtBetween(from, to, limit, 0)
        val countByStatus = jobs.groupingBy { it.status }.eachCount()
        val byDay = jobs.groupingBy { it.createdAt.atZone(ZoneOffset.UTC).toLocalDate() }
            .eachCount()
            .toSortedMap()
            .mapKeys { it.key.format(DateTimeFormatter.ISO_LOCAL_DATE) }
            .map { (k, v) -> DayCount(k, v) }

        var totalProcessedFiles = 0
        var totalFailedFiles = 0
        var totalSkippedFiles = 0
        var totalProjects = 0
        val statusCounts = mutableMapOf<String, Int>().withDefault { 0 }
        JobStatus.entries.forEach { statusCounts[it.name] = 0 }
        countByStatus.forEach { (k, v) -> statusCounts[k.name] = v }

        jobs.forEach { job ->
            totalProcessedFiles += job.progress.processedFiles
            totalFailedFiles += job.progress.failedFiles
            totalSkippedFiles += job.progress.skippedFiles
            totalProjects += job.progress.totalProjects
        }

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

        val errorCodeCounts = mutableMapOf<String, Int>().withDefault { 0 }
        jobs.flatMap { it.items }.filter { it.error != null }.forEach { item ->
            val code = item.error!!.code.name
            errorCodeCounts[code] = errorCodeCounts.getValue(code) + 1
        }

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
