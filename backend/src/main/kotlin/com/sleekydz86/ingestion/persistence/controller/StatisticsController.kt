package com.sleekydz86.ingestion.persistence.controller


import com.sleekydz86.ingestion.application.usecase.ExportStatisticsUseCase
import com.sleekydz86.ingestion.application.usecase.GetStatisticsUseCase
import com.sleekydz86.ingestion.application.usecase.StatisticsResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/v1/statistics")
class StatisticsController(
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val exportStatisticsUseCase: ExportStatisticsUseCase,
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getStatistics(
        @RequestParam from: String,
        @RequestParam to: String,
        @RequestParam(required = false, defaultValue = "10000") limit: Int,
    ): ResponseEntity<StatisticsResponse> {
        val fromInstant = Instant.parse(from)
        val toInstant = Instant.parse(to)
        if (fromInstant.isAfter(toInstant)) {
            return ResponseEntity.badRequest().build()
        }
        val response = getStatisticsUseCase.execute(fromInstant, toInstant, limit.coerceIn(1, 50_000))
        return ResponseEntity.ok(response)
    }

    @GetMapping(
        value = ["/export"],
        produces = ["application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"],
    )
    fun exportExcel(
        @RequestParam from: String,
        @RequestParam to: String,
        @RequestParam(required = false, defaultValue = "10000") limit: Int,
    ): ResponseEntity<ByteArray> {
        val fromInstant = Instant.parse(from)
        val toInstant = Instant.parse(to)
        if (fromInstant.isAfter(toInstant)) {
            return ResponseEntity.badRequest().build()
        }
        val bytes = exportStatisticsUseCase.execute(fromInstant, toInstant, limit.coerceIn(1, 50_000))
        val filename = "ingestion-statistics-${DateTimeFormatter.ISO_LOCAL_DATE.format(fromInstant.atZone(java.time.ZoneOffset.UTC))}-${DateTimeFormatter.ISO_LOCAL_DATE.format(toInstant.atZone(java.time.ZoneOffset.UTC))}.xlsx"
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(bytes)
    }
}
