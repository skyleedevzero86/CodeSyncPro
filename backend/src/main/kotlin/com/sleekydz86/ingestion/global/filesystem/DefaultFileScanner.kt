package com.sleekydz86.ingestion.global.filesystem

import com.sleekydz86.ingestion.domain.port.FileScanner
import com.sleekydz86.ingestion.domain.port.ScannedFile
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

@Component
class DefaultFileScanner : FileScanner {

    private val binaryDetector = BinaryDetector()
    private val pathFilter = PathFilter()

    override fun scanFiles(
        repositoryDirectory: Path,
        fileFilters: com.sleekydz86.ingestion.domain.model.FileFilters,
        changedFilePaths: Set<String>?,
    ): List<ScannedFile> {
        val candidates = if (changedFilePaths != null) {
            changedFilePaths.asSequence()
                .map { repositoryDirectory.resolve(it) }
                .filter { Files.exists(it) }
        } else {
            Files.walk(repositoryDirectory).use { stream ->
                stream.toList().asSequence()
            }
        }

        return candidates
            .filter { Files.isRegularFile(it) }
            .filter { pathFilter.isAllowed(repositoryDirectory, it, fileFilters) }
            .filter { Files.size(it) <= fileFilters.maxFileSizeBytes }
            .filter { !fileFilters.skipBinary || !binaryDetector.isBinary(it) }
            .map { file ->
                val content = Files.readString(file, StandardCharsets.UTF_8)
                val relative = repositoryDirectory.relativize(file).toString().replace('\\', '/')
                ScannedFile(
                    filePath = relative,
                    absolutePath = file,
                    content = content,
                    sizeBytes = Files.size(file),
                )
            }
            .toList()
    }
}
