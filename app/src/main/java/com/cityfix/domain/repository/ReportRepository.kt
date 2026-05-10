package com.cityfix.domain.repository

import android.net.Uri
import com.cityfix.domain.model.Comment
import com.cityfix.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getAllReports(): Flow<List<Report>>
    fun getReportById(id: String): Flow<Report?>
    fun getFilteredReports(category: String?, status: String?): Flow<List<Report>>
    fun getReportsByUser(userId: String): Flow<List<Report>>
    fun getReportsCountByStatus(status: String): Flow<Int>
    suspend fun createReport(report: Report, imageUri: Uri?): String
    suspend fun updateReport(report: Report)
    suspend fun deleteReport(id: String)
    suspend fun refreshReports()
    suspend fun voteReport(reportId: String, userId: String, hasVoted: Boolean)

    fun getComments(reportId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment)
    suspend fun deleteComment(commentId: String, reportId: String)
}
