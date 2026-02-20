package com.sleekydz86.ingestion.global.filesystem

import com.sleekydz86.ingestion.domain.model.FileFilters
import java.nio.file.FileSystems
import java.nio.file.Path

class PathFilter {

    private val defaultExcludedDirs = DefaultExclusions.DIRS.map { it.removePrefix("./").removeSuffix("/") }
    private val defaultExcludedFiles = DefaultExclusions.FILES

    fun isAllowed(root: Path, file: Path, filters: FileFilters): Boolean {
        val relative = root.relativize(file).toString().replace('\\', '/')
        val path = Path.of(relative)

        val allExcludedDirs = (defaultExcludedDirs + filters.excludeDirs).distinct()
        if (allExcludedDirs.any { dir ->
                val normalized = relative.trim('/')
                normalized == dir || normalized.startsWith("$dir/") || normalized.contains("/$dir/")
            }) {
            return false
        }

        val allExcludedFiles = (defaultExcludedFiles + filters.excludeFiles).distinct()
        if (allExcludedFiles.any { pattern ->
                matchesGlob(path, pattern)
            }) {
            return false
        }

        if (filters.includeGlobs.isNotEmpty()) {
            if (filters.includeGlobs.none { glob ->
                    matchesGlob(path, glob)
                }) {
                return false
            }
        }

        return true
    }

    private fun matchesGlob(path: Path, pattern: String): Boolean {
        val matcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")
        return matcher.matches(path) || matcher.matches(path.fileName ?: path)
    }
}
