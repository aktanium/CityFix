package com.cityfix.domain.usecase

import android.net.Uri
import com.cityfix.domain.model.Report
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
    operator fun invoke(id: String): Flow<Report?> = repository.getReportById(id)
}

class GetFilteredReportsUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(
        category: String? = null,
        status: String? = null
    ): Flow<List<Report>> = repository.getFilteredReports(category, status)
}

class CreateReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(report: Report, imageUri: Uri?): Result<String> = runCatching {
        require(report.title.isNotBlank()) { "Title cannot be empty" }
        require(report.description.isNotBlank()) { "Description cannot be empty" }
        repository.createReport(report, imageUri)
    }
}

class UpdateReportStatusUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(report: Report, newStatus: String): Result<Unit> =
        runCatching {
            repository.updateReport(report.copy(status = newStatus))
        }
}

class DeleteReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = runCatching {
        repository.deleteReport(id)
    }
}

class RefreshReportsUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        repository.refreshReports()
    }
}

class VoteReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(
        reportId: String,
        userId: String,
        hasVoted: Boolean
    ): Result<Unit> = runCatching {
        repository.voteReport(reportId, userId, hasVoted)
    }
}
