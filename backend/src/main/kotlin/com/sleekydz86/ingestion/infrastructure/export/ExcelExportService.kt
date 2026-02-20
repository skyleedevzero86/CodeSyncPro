package com.sleekydz86.ingestion.infrastructure.export


import com.sleekydz86.ingestion.domain.model.Job
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneOffset
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class ExcelExportService {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC)

    fun exportJobs(jobs: List<Job>, from: Instant, to: Instant): ByteArray {
        XSSFWorkbook().use { wb ->
            createSummarySheet(wb, jobs, from, to)
            createJobListSheet(wb, jobs)
            createItemDetailSheet(wb, jobs)
            val out = ByteArrayOutputStream()
            wb.write(out)
            return out.toByteArray()
        }
    }

    private fun createSummarySheet(wb: XSSFWorkbook, jobs: List<Job>, from: Instant, to: Instant) {
        val sheet = wb.createSheet("요약")
        val headerStyle = wb.createCellStyle().apply {
            setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index)
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        var rowNum = 0
        sheet.createRow(rowNum++).apply {
            createCell(0).setCellValue("항목")
            createCell(1).setCellValue("값")
            getCell(0).cellStyle = headerStyle
            getCell(1).cellStyle = headerStyle
        }
        listOf(
            "조회 기간 (from)" to dateFormatter.format(from),
            "조회 기간 (to)" to dateFormatter.format(to),
            "총 작업 수" to jobs.size.toString(),
            "총 처리 파일 수" to jobs.sumOf { it.progress.processedFiles }.toString(),
            "총 실패 파일 수" to jobs.sumOf { it.progress.failedFiles }.toString(),
            "총 건너뜀 파일 수" to jobs.sumOf { it.progress.skippedFiles }.toString(),
            "총 프로젝트 수" to jobs.sumOf { it.progress.totalProjects }.toString(),
        ).forEach { (label, value) ->
            sheet.createRow(rowNum++).apply {
                createCell(0).setCellValue(label)
                createCell(1).setCellValue(value)
            }
        }
        rowNum++
        sheet.createRow(rowNum++).apply {
            createCell(0).setCellValue("상태별 작업 수")
            getCell(0).cellStyle = headerStyle
        }
        jobs.groupingBy { it.status.name }.eachCount().forEach { (status, count) ->
            sheet.createRow(rowNum++).apply {
                createCell(0).setCellValue(status)
                createCell(1).setCellValue(count.toLong())
            }
        }
        sheet.setColumnWidth(0, 6000)
        sheet.setColumnWidth(1, 5000)
    }

    private fun createJobListSheet(wb: XSSFWorkbook, jobs: List<Job>) {
        val sheet = wb.createSheet("작업 목록")
        val headerStyle = wb.createCellStyle().apply {
            setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index)
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        val headers = listOf(
            "작업 ID", "상태", "소스타입", "모드", "생성일시", "시작일시", "완료일시",
            "총 프로젝트", "처리 프로젝트", "총 파일", "처리 파일", "실패 파일", "건너뜀", "항목 수"
        )
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h); headerRow.getCell(i).cellStyle = headerStyle }
        jobs.forEachIndexed { idx, job ->
            val row = sheet.createRow(idx + 1)
            row.createCell(0).setCellValue(job.id.value)
            row.createCell(1).setCellValue(job.status.name)
            row.createCell(2).setCellValue(job.sourceType.name)
            row.createCell(3).setCellValue(job.options.mode.name)
            row.createCell(4).setCellValue(job.createdAt.toString())
            row.createCell(5).setCellValue(job.startedAt?.toString() ?: "")
            row.createCell(6).setCellValue(job.completedAt?.toString() ?: "")
            row.createCell(7).setCellValue(job.progress.totalProjects.toLong())
            row.createCell(8).setCellValue(job.progress.processedProjects.toLong())
            row.createCell(9).setCellValue(job.progress.totalFiles.toLong())
            row.createCell(10).setCellValue(job.progress.processedFiles.toLong())
            row.createCell(11).setCellValue(job.progress.failedFiles.toLong())
            row.createCell(12).setCellValue(job.progress.skippedFiles.toLong())
            row.createCell(13).setCellValue(job.items.size.toLong())
        }
        (0..13).forEach { sheet.autoSizeColumn(it) }
    }

    private fun createItemDetailSheet(wb: XSSFWorkbook, jobs: List<Job>) {
        val sheet = wb.createSheet("항목 상세")
        val headerStyle = wb.createCellStyle().apply {
            setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index)
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        val headers = listOf("작업 ID", "항목 ID", "프로젝트 경로", "파일 경로", "상태", "에러 코드", "에러 메시지", "재시도 횟수")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h); headerRow.getCell(i).cellStyle = headerStyle }
        var rowNum = 1
        jobs.forEach { job ->
            job.items.forEach { item ->
                val row = sheet.createRow(rowNum++)
                row.createCell(0).setCellValue(job.id.value)
                row.createCell(1).setCellValue(item.id.value)
                row.createCell(2).setCellValue(item.projectPath)
                row.createCell(3).setCellValue(item.filePath)
                row.createCell(4).setCellValue(item.status.name)
                row.createCell(5).setCellValue(item.error?.code?.name ?: "")
                row.createCell(6).setCellValue(item.error?.message ?: "")
                row.createCell(7).setCellValue(item.retryCount.toLong())
            }
        }
        (0..7).forEach { sheet.autoSizeColumn(it) }
    }
}
