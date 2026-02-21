package com.sleekydz86.ingestion.global.filesystem

import java.nio.file.Files
import java.nio.file.Path

class BinaryDetector(
    private val sampleSize: Int = 1024,
) {
    fun isBinary(path: Path): Boolean {
        val size = Files.size(path)
        if (size == 0L) return false

        val bytes = Files.newInputStream(path).use { input ->
            val buffer = ByteArray(sampleSize)
            val read = input.read(buffer)
            if (read <= 0) {
                ByteArray(0)
            } else {
                buffer.copyOf(read)
            }
        }
        return bytes.any { it == 0.toByte() }
    }
}
