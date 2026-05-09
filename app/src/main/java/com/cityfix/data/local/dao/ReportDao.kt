package com.cityfix.data.local.dao

import androidx.room.*
import com.cityfix.domain.model.Report
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE id = :id")
    fun getReportById(id: String): Flow<Report?>

    @Query("SELECT * FROM reports WHERE category = :category ORDER BY createdAt DESC")
    fun getReportsByCategory(category: String): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE status = :status ORDER BY createdAt DESC")
    fun getReportsByStatus(status: String): Flow<List<Report>>

    @Query(
        """
        SELECT * FROM reports 
        WHERE (:category IS NULL OR category = :category)
        AND (:status IS NULL OR status = :status)
        ORDER BY createdAt DESC
    """
    )
    fun getFilteredReports(category: String?, status: String?): Flow<List<Report>>

    @Query("SELECT * FROM reports WHERE userId = :userId ORDER BY createdAt DESC")
    fun getReportsByUser(userId: String): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: Report)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<Report>)

    @Update
    suspend fun updateReport(report: Report)

    @Delete
    suspend fun deleteReport(report: Report)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: String)

    @Query("SELECT COUNT(*) FROM reports")
    fun getTotalReportsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reports WHERE status = :status")
    fun getReportsCountByStatus(status: String): Flow<Int>
}
