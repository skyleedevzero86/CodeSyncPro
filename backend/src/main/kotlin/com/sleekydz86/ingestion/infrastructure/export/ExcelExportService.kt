package com.sleekydz86.ingestion.infrastructure.export


import com.sleekydz86.ingestion.domain.model.Job
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneOffset
import org.springframework.stereotype.Component
import org.apache.poi.ss.usermodel.Cell
import java.time.format.DateTimeFormatter

@Component
class ExcelExportService {

    private fun Cell.setCellValueAny(value: Any) {
        when (value) {
            is Double -> setCellValue(value)
            is Number -> setCellValue(value.toDouble())
            else -> setCellValue(value.toString())
        }
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC)

    fun exportJobs(jobs: List<Job>, from: Instant, to: Instant): ByteArray =
        XSSFWorkbook().use { wb ->
            createSummarySheet(wb, jobs, from, to)
            createJobListSheet(wb, jobs)
            createItemDetailSheet(wb, jobs)
            ByteArrayOutputStream().also { wb.write(it) }.toByteArray()
        }

    private fun headerStyle(wb: XSSFWorkbook) =
        wb.createCellStyle().apply {
            setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index)
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

    private fun createSummarySheet(wb: XSSFWorkbook, jobs: List<Job>, from: Instant, to: Instant) {
        val sheet = wb.createSheet("요약")
        val style = headerStyle(wb)
        val summaryData = listOf(
            "조회 기간 (from)" to dateFormatter.format(from),
            "조회 기간 (to)" to dateFormatter.format(to),
            "총 작업 수" to jobs.size.toString(),
            "총 처리 파일 수" to jobs.sumOf { it.progress.processedFiles }.toString(),
            "총 실패 파일 수" to jobs.sumOf { it.progress.failedFiles }.toString(),
            "총 건너뜀 파일 수" to jobs.sumOf { it.progress.skippedFiles }.toString(),
            "총 프로젝트 수" to jobs.sumOf { it.progress.totalProjects }.toString(),
        )
        sheet.createRow(0).apply {
            createCell(0).setCellValue("항목")
            createCell(1).setCellValue("값")
            getCell(0).cellStyle = style
            getCell(1).cellStyle = style
        }
        summaryData.forEachIndexed { rowNum, (label, value) ->
            sheet.createRow(rowNum + 1).apply {
                createCell(0).setCellValue(label)
                createCell(1).setCellValue(value)
            }
        }
        var nextRow = summaryData.size + 2
        sheet.createRow(nextRow++).apply {
            createCell(0).setCellValue("상태별 작업 수")
            getCell(0).cellStyle = style
        }
        jobs.groupingBy { it.status.name }.eachCount().forEach { (status, count) ->
            sheet.createRow(nextRow++).apply {
                createCell(0).setCellValue(status)
                createCell(1).setCellValue(count.toDouble())
            }
        }
        sheet.setColumnWidth(0, 6000)
        sheet.setColumnWidth(1, 5000)
    }

    private fun createJobListSheet(wb: XSSFWorkbook, jobs: List<Job>) {
        val sheet = wb.createSheet("작업 목록")
        val style = headerStyle(wb)
        val headers = listOf(
            "작업 ID", "상태", "소스타입", "모드", "생성일시", "시작일시", "완료일시",
            "총 프로젝트", "처리 프로젝트", "총 파일", "처리 파일", "실패 파일", "건너뜀", "항목 수"
        )
        sheet.createRow(0).also { row ->
            headers.forEachIndexed { i, h ->
                row.createCell(i).setCellValue(h)
                row.getCell(i).cellStyle = style
            }
        }
        jobs.forEachIndexed { idx, job ->
            sheet.createRow(idx + 1).apply {
                listOf(
                    job.id.value,
                    job.status.name,
                    job.sourceType.name,
                    job.options.mode.name,
                    job.createdAt.toString(),
                    job.startedAt?.toString() ?: "",
                    job.completedAt?.toString() ?: "",
                    job.progress.totalProjects.toDouble(),
                    job.progress.processedProjects.toDouble(),
                    job.progress.totalFiles.toDouble(),
                    job.progress.processedFiles.toDouble(),
                    job.progress.failedFiles.toDouble(),
                    job.progress.skippedFiles.toDouble(),
                    job.items.size.toDouble(),
                ).forEachIndexed { col, value ->
                    createCell(col).setCellValueAny(value)
                }
            }
        }
        (0..13).forEach { sheet.autoSizeColumn(it) }
    }

    private fun createItemDetailSheet(wb: XSSFWorkbook, jobs: List<Job>) {
        val sheet = wb.createSheet("항목 상세")
        val style = headerStyle(wb)
        val headers = listOf("작업 ID", "항목 ID", "프로젝트 경로", "파일 경로", "상태", "에러 코드", "에러 메시지", "재시도 횟수")
        sheet.createRow(0).also { row ->
            headers.forEachIndexed { i, h ->
                row.createCell(i).setCellValue(h)
                row.getCell(i).cellStyle = style
            }
        }
        val itemRows = jobs.flatMap { job ->
            job.items.map { item ->
                listOf(
                    job.id.value,
                    item.id.value,
                    item.projectPath,
                    item.filePath,
                    item.status.name,
                    item.error?.code?.name ?: "",
                    item.error?.message ?: "",
                    item.retryCount.toDouble(),
                )
            }
        }
        itemRows.forEachIndexed { idx, cells ->
            sheet.createRow(idx + 1).apply {
                cells.forEachIndexed { col, value ->
                    createCell(col).setCellValueAny(value)
                }
            }
        }
        (0..7).forEach { sheet.autoSizeColumn(it) }
    }
}
