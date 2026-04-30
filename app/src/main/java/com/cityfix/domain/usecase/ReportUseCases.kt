package com.cityfix.domain.usecase

import com.cityfix.domain.model.Report
import com.cityfix.domain.model.ReportCategory
import com.cityfix.domain.model.ReportStatus
import com.cityfix.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllReportsUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(): Flow<List<Report>> = repository.getAllReports()
}

class GetReportByIdUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(id: Long): Flow<Report?> = repository.getReportById(id)
}

class GetFilteredReportsUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(
        category: ReportCategory? = null,
        status: ReportStatus? = null
    ): Flow<List<Report>> = repository.getFilteredReports(category, status)
}

class CreateReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(report: Report): Result<Long> = runCatching {
        require(report.title.isNotBlank()) { "Title cannot be empty" }
        require(report.description.isNotBlank()) { "Description cannot be empty" }
        repository.createReport(report)
    }
}

class UpdateReportStatusUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(report: Report, newStatus: ReportStatus): Result<Unit> =
        runCatching {
            repository.updateReport(report.copy(status = newStatus))
        }
}

class DeleteReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> = runCatching {
        repository.deleteReport(id)
    }
}
