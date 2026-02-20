package com.sleekydz86.ingestion.persistence.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.sleekydz86.ingestion.domain.model.IngestMode
import com.sleekydz86.ingestion.domain.model.JobStatus
import com.sleekydz86.ingestion.domain.model.SourceType
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class CreateJobRequest(
    @field:NotNull
    val sourceType: SourceType,

    @field:NotNull
    @field:Valid
    val sourceConfig: SourceConfigDto,

    @field:NotNull
    @field:Valid
    val options: JobOptionsDto,

    val callbackUrl: String? = null,
)

data class SourceConfigDto(
    @field:NotBlank
    val baseUrl: String,

    @field:NotBlank
    val accessToken: String,

    val projectIds: List<Long> = emptyList(),
    val groupIds: List<Long> = emptyList(),
    val targetBranch: String = "main",
    val shouldIncludeSubgroups: Boolean = true,
    val shouldIncludeArchived: Boolean = false,
    val shouldUseMembershipOnly: Boolean = true,
    val pageSize: Int = 100,
)

data class JobOptionsDto(
    val mode: IngestMode,

    @field:Valid
    val fileFilters: FileFiltersDto,

    @field:Valid
    val concurrency: ConcurrencyConfigDto,

    val cleanupAfterIngest: Boolean = true,
    val since: Instant? = null,
)

data class FileFiltersDto(
    val includeGlobs: List<String> = emptyList(),
    val excludeDirs: List<String> = emptyList(),
    val excludeFiles: List<String> = emptyList(),
    val maxFileSizeBytes: Long = 5_000_000,
    val skipBinary: Boolean = true,
)

data class ConcurrencyConfigDto(
    val projects: Int = 2,
    val files: Int = 8,
)

data class RetryRequest(
    val itemIds: List<String>? = null,
)

data class CreateJobResponse(
    val jobId: String,
    val status: JobStatus,
    val createdAt: Instant,
    @JsonProperty("statusUrl")
    val statusUrl: String,
)
