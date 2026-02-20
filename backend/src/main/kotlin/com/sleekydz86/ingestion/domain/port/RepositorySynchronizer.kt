package com.sleekydz86.ingestion.domain.port

import com.sleekydz86.ingestion.domain.model.SourceConfig
import java.nio.file.Path

interface RepositorySynchronizer {
    fun syncRepository(
        sourceConfig: SourceConfig,
        projectPath: String,
        targetDirectory: Path,
        mode: String,
    ): SyncResult
}

data class SyncResult(
    val repositoryDirectory: Path,
    val isUpdated: Boolean,
    val changedFilePaths: Set<String>?,
    val currentCommitSha: String?,
    val branchName: String,
)
