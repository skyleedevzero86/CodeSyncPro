package com.sleekydz86.ingestion.domain.port

import com.sleekydz86.ingestion.domain.model.SourceConfig

interface EmbeddingClient {
    suspend fun upsertDocument(
        sourceConfig: SourceConfig,
        projectPath: String,
        filePath: String,
        content: String,
        commitSha: String?,
        branchName: String,
    )
}
