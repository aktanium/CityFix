package com.cityfix.domain.repository

import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getAllReports(): Flow<List<Report>>
    fun getReportById(id: Long): Flow<Report?>
    fun getFilteredReports(category: ReportCategory?, status: ReportStatus?): Flow<List<Report>>
    fun getReportsCountByStatus(status: ReportStatus): Flow<Int>
    suspend fun createReport(report: Report): Long
    suspend fun updateReport(report: Report)
    suspend fun deleteReport(id: Long)
}
