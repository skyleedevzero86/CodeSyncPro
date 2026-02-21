package com.sleekydz86.ingestion.infrastructure.persistence.mapper

import com.sleekydz86.ingestion.domain.model.ConcurrencyConfig
import com.sleekydz86.ingestion.domain.model.ErrorCode
import com.sleekydz86.ingestion.domain.model.FileFilters
import com.sleekydz86.ingestion.domain.model.IngestMode
import com.sleekydz86.ingestion.domain.model.ItemError
import com.sleekydz86.ingestion.domain.model.ItemId
import com.sleekydz86.ingestion.domain.model.ItemStatus
import com.sleekydz86.ingestion.domain.model.Job
import com.sleekydz86.ingestion.domain.model.JobId
import com.sleekydz86.ingestion.domain.model.JobItem
import com.sleekydz86.ingestion.domain.model.JobOptions
import com.sleekydz86.ingestion.domain.model.JobProgress
import com.sleekydz86.ingestion.domain.model.JobStatus
import com.sleekydz86.ingestion.domain.model.SourceConfig
import com.sleekydz86.ingestion.domain.model.SourceType
import com.sleekydz86.ingestion.infrastructure.persistence.JobDocument
import com.sleekydz86.ingestion.infrastructure.persistence.JobItemDocument
import com.sleekydz86.ingestion.infrastructure.persistence.JobProgressDocument
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class JobMapper {

    fun toDocument(job: Job): JobDocument {
        return JobDocument(
            id = job.id.value,
            sourceType = job.sourceType.name,
            sourceConfig = mapOf(
                "baseUrl" to job.sourceConfig.baseUrl,
                "accessToken" to job.sourceConfig.accessToken,
                "projectIds" to job.sourceConfig.projectIds,
                "groupIds" to job.sourceConfig.groupIds,
                "targetBranch" to job.sourceConfig.targetBranch,
                "shouldIncludeSubgroups" to job.sourceConfig.shouldIncludeSubgroups,
                "shouldIncludeArchived" to job.sourceConfig.shouldIncludeArchived,
                "shouldUseMembershipOnly" to job.sourceConfig.shouldUseMembershipOnly,
                "pageSize" to job.sourceConfig.pageSize,
            ),
            options = mapOf(
                "mode" to job.options.mode.name,
                "fileFilters" to mapOf(
                    "includeGlobs" to job.options.fileFilters.includeGlobs,
                    "excludeDirs" to job.options.fileFilters.excludeDirs,
                    "excludeFiles" to job.options.fileFilters.excludeFiles,
                    "maxFileSizeBytes" to job.options.fileFilters.maxFileSizeBytes,
                    "skipBinary" to job.options.fileFilters.skipBinary,
                ),
                "concurrency" to mapOf(
                    "projects" to job.options.concurrency.projects,
                    "files" to job.options.concurrency.files,
                ),
                "cleanupAfterIngest" to job.options.cleanupAfterIngest,
                "since" to job.options.since?.toString(),
            ),
            status = job.status.name,
            progress = toProgressDocument(job.progress),
            createdAt = job.createdAt,
            startedAt = job.startedAt,
            completedAt = job.completedAt,
            cancelledAt = job.cancelledAt,
            callbackUrl = job.callbackUrl,
            items = job.items.map { toItemDocument(it) },
            metadata = job.metadata,
        )
    }

    fun toDomain(document: JobDocument): Job {
        return Job(
            id = JobId(document.id),
            sourceType = SourceType.valueOf(document.sourceType),
            sourceConfig = SourceConfig(
                baseUrl = document.sourceConfig["baseUrl"] as String,
                accessToken = document.sourceConfig["accessToken"] as String,
                projectIds = (document.sourceConfig["projectIds"] as? List<*>)?.mapNotNull { (it as? Number)?.toLong() }
                    ?: emptyList(),
                groupIds = (document.sourceConfig["groupIds"] as? List<*>)?.mapNotNull { (it as? Number)?.toLong() }
                    ?: emptyList(),
                targetBranch = document.sourceConfig["targetBranch"] as? String ?: "main",
                shouldIncludeSubgroups = document.sourceConfig["shouldIncludeSubgroups"] as? Boolean ?: true,
                shouldIncludeArchived = document.sourceConfig["shouldIncludeArchived"] as? Boolean ?: false,
                shouldUseMembershipOnly = document.sourceConfig["shouldUseMembershipOnly"] as? Boolean ?: true,
                pageSize = (document.sourceConfig["pageSize"] as? Number)?.toInt() ?: 100,
            ),
            options = parseOptions(document.options),
            status = JobStatus.valueOf(document.status),
            progress = toProgressDomain(document.progress),
            createdAt = document.createdAt,
            startedAt = document.startedAt,
            completedAt = document.completedAt,
            cancelledAt = document.cancelledAt,
            callbackUrl = document.callbackUrl,
            items = document.items.map { toItemDomain(it, JobId(document.id)) },
            metadata = document.metadata,
        )
    }

    fun toProgressDocument(progress: JobProgress): JobProgressDocument {
        return JobProgressDocument(
            totalProjects = progress.totalProjects,
            processedProjects = progress.processedProjects,
            totalFiles = progress.totalFiles,
            processedFiles = progress.processedFiles,
            failedFiles = progress.failedFiles,
            skippedFiles = progress.skippedFiles,
        )
    }

    fun toProgressDomain(document: JobProgressDocument): JobProgress {
        return JobProgress(
            totalProjects = document.totalProjects,
            processedProjects = document.processedProjects,
            totalFiles = document.totalFiles,
            processedFiles = document.processedFiles,
            failedFiles = document.failedFiles,
            skippedFiles = document.skippedFiles,
        )
    }

    fun toItemDocument(item: JobItem): JobItemDocument {
        return JobItemDocument(
            id = item.id.value,
            projectPath = item.projectPath,
            filePath = item.filePath,
            status = item.status.name,
            error = item.error?.let { error ->
                mapOf(
                    "code" to error.code.name,
                    "message" to error.message,
                    "retryable" to error.retryable,
                    "occurredAt" to error.occurredAt.toString(),
                )
            },
            retryCount = item.retryCount,
            maxRetries = item.maxRetries,
            nextRetryAt = item.nextRetryAt,
            processedAt = item.processedAt,
            metadata = item.metadata,
        )
    }

    fun toItemDomain(document: JobItemDocument, jobId: JobId): JobItem {
        return JobItem(
            id = ItemId(document.id),
            jobId = jobId,
            projectPath = document.projectPath,
            filePath = document.filePath,
            status = ItemStatus.valueOf(document.status),
            error = document.error?.let { errorMap ->
                ItemError(
                    code = ErrorCode.valueOf(errorMap["code"] as String),
                    message = errorMap["message"] as String,
                    retryable = errorMap["retryable"] as Boolean,
                    occurredAt = Instant.parse(errorMap["occurredAt"] as String),
                )
            },
            retryCount = document.retryCount,
            maxRetries = document.maxRetries,
            nextRetryAt = document.nextRetryAt,
            processedAt = document.processedAt,
            metadata = document.metadata,
        )
    }

    private fun parseOptions(options: Map<String, Any?>): JobOptions {
        val fileFiltersMap = options["fileFilters"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val concurrencyMap = options["concurrency"] as? Map<*, *> ?: emptyMap<Any, Any>()

        return JobOptions(
            mode = IngestMode.valueOf(options["mode"] as? String ?: "FULL"),
            fileFilters = FileFilters(
                includeGlobs = (fileFiltersMap["includeGlobs"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList(),
                excludeDirs = (fileFiltersMap["excludeDirs"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                excludeFiles = (fileFiltersMap["excludeFiles"] as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList(),
                maxFileSizeBytes = (fileFiltersMap["maxFileSizeBytes"] as? Number)?.toLong() ?: 5_000_000,
                skipBinary = fileFiltersMap["skipBinary"] as? Boolean ?: true,
            ),
            concurrency = ConcurrencyConfig(
                projects = (concurrencyMap["projects"] as? Number)?.toInt() ?: 2,
                files = (concurrencyMap["files"] as? Number)?.toInt() ?: 8,
            ),
            cleanupAfterIngest = options["cleanupAfterIngest"] as? Boolean ?: true,
            since = (options["since"] as? String)?.let { Instant.parse(it) },
        )
    }
}
