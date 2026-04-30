package com.cityfix.data.repository

import com.cityfix.data.local.dao.ReportDao
import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import com.cityfix.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportDao: ReportDao
) : ReportRepository {

    override fun getAllReports(): Flow<List<Report>> =
        reportDao.getAllReports().map { entities -> entities.map { it.toDomain() } }

    override fun getReportById(id: Long): Flow<Report?> =
        reportDao.getReportById(id).map { it?.toDomain() }

    override fun getFilteredReports(
        category: ReportCategory?,
        status: ReportStatus?
    ): Flow<List<Report>> =
        reportDao.getFilteredReports(
            category = category?.name,
            status = status?.name
        ).map { entities -> entities.map { it.toDomain() } }

    override fun getReportsCountByStatus(status: ReportStatus): Flow<Int> =
        reportDao.getReportsCountByStatus(status.name)

    override suspend fun createReport(report: Report): Long =
        reportDao.insertReport(report.toEntity())

    override suspend fun updateReport(report: Report) =
        reportDao.updateReport(report.toEntity())

    override suspend fun deleteReport(id: Long) =
        reportDao.deleteReportById(id)
}
