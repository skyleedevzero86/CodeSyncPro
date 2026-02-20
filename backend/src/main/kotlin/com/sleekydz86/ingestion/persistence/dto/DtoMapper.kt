package com.sleekydz86.ingestion.persistence.dto

import com.sleekydz86.ingestion.domain.model.ConcurrencyConfig
import com.sleekydz86.ingestion.domain.model.FileFilters
import com.sleekydz86.ingestion.domain.model.JobOptions


object DtoMapper {

    fun toUseCaseRequest(dto: DtoCreateJobRequest): UseCaseCreateJobRequest {
        return UseCaseCreateJobRequest(
            sourceType = dto.sourceType,
            sourceConfig = SourceConfig(
                baseUrl = dto.sourceConfig.baseUrl,
                accessToken = dto.sourceConfig.accessToken,
                projectIds = dto.sourceConfig.projectIds,
                groupIds = dto.sourceConfig.groupIds,
                targetBranch = dto.sourceConfig.targetBranch,
                shouldIncludeSubgroups = dto.sourceConfig.shouldIncludeSubgroups,
                shouldIncludeArchived = dto.sourceConfig.shouldIncludeArchived,
                shouldUseMembershipOnly = dto.sourceConfig.shouldUseMembershipOnly,
                pageSize = dto.sourceConfig.pageSize,
            ),
            options = JobOptions(
                mode = dto.options.mode,
                fileFilters = FileFilters(
                    includeGlobs = dto.options.fileFilters.includeGlobs,
                    excludeDirs = dto.options.fileFilters.excludeDirs,
                    excludeFiles = dto.options.fileFilters.excludeFiles,
                    maxFileSizeBytes = dto.options.fileFilters.maxFileSizeBytes,
                    skipBinary = dto.options.fileFilters.skipBinary,
                ),
                concurrency = ConcurrencyConfig(
                    projects = dto.options.concurrency.projects,
                    files = dto.options.concurrency.files,
                ),
                cleanupAfterIngest = dto.options.cleanupAfterIngest,
                since = dto.options.since,
            ),
            callbackUrl = dto.callbackUrl,
        )
    }
}
