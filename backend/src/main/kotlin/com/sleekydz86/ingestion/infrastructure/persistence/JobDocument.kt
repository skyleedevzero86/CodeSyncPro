package com.sleekydz86.ingestion.infrastructure.persistence


import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "jobs")
data class JobDocument(
    @Id
    val id: String,
    val sourceType: String,
    val sourceConfig: Map<String, Any?>,
    val options: Map<String, Any?>,
    val status: String,
    val progress: JobProgressDocument,
    val createdAt: Instant,
    val startedAt: Instant?,
    val completedAt: Instant?,
    val cancelledAt: Instant?,
    val callbackUrl: String?,
    val items: List<JobItemDocument>,
    val metadata: Map<String, String>,
)

data class JobProgressDocument(
    val totalProjects: Int,
    val processedProjects: Int,
    val totalFiles: Int,
    val processedFiles: Int,
    val failedFiles: Int,
    val skippedFiles: Int = 0,
)

data class JobItemDocument(
    val id: String,
    val projectPath: String,
    val filePath: String,
    val status: String,
    val error: Map<String, Any?>?,
    val retryCount: Int,
    val maxRetries: Int,
    val nextRetryAt: Instant?,
    val processedAt: Instant?,
    val metadata: Map<String, String>,
)
