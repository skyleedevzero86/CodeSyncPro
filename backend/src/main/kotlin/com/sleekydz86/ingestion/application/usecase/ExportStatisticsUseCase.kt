package com.sleekydz86.ingestion.application.usecase

import com.sleekydz86.ingestion.domain.port.JobRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ExportStatisticsUseCase(
    private val jobRepository: JobRepository,
    private val excelExportService: ExcelExportService,
) {
    fun execute(from: Instant, to: Instant, limit: Int = 10_000): ByteArray {
        val jobs = jobRepository.findByCreatedAtBetween(from, to, limit, 0)
        return excelExportService.exportJobs(jobs, from, to)
    }
}
