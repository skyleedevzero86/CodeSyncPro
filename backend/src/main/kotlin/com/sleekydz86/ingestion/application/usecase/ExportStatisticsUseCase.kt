package com.sleekydz86.ingestion.application.usecase

import com.sleekydz86.ingestion.domain.port.JobRepository
import com.sleekydz86.ingestion.infrastructure.export.ExcelExportService
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ExportStatisticsUseCase(
    private val jobRepository: JobRepository,
    private val excelExportService: ExcelExportService,
) {
    fun execute(from: Instant, to: Instant, limit: Int = 10_000): ByteArray =
        excelExportService.exportJobs(
            jobRepository.findByCreatedAtBetween(from, to, limit, 0),
            from,
            to,
        )
}
