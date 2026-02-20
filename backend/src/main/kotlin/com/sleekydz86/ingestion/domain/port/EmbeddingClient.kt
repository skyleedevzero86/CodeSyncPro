package com.sleekydz86.ingestion.domain.port

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
