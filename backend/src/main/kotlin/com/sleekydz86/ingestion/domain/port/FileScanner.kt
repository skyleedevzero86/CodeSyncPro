package com.sleekydz86.ingestion.domain.port

import java.nio.file.Path

interface FileScanner {
    fun scanFiles(
        repositoryDirectory: Path,
        fileFilters: com.sleekydz86.ingestion.domain.model.FileFilters,
        changedFilePaths: Set<String>?,
    ): List<ScannedFile>
}

data class ScannedFile(
    val filePath: String,
    val absolutePath: Path,
    val content: String,
    val sizeBytes: Long,
)
