package com.cityfix.data.local.dao

import androidx.room.*
import com.cityfix.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    @Query("SELECT * FROM reports WHERE status != 'DELETED' ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id")
    fun getReportById(id: String): Flow<ReportEntity?>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getReportByIdOnce(id: String): ReportEntity?

    @Query("SELECT * FROM reports WHERE category = :category AND status != 'DELETED' ORDER BY createdAt DESC")
    fun getReportsByCategory(category: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status = :status AND status != 'DELETED' ORDER BY createdAt DESC")
    fun getReportsByStatus(status: String): Flow<List<ReportEntity>>

    @Query(
        """
        SELECT * FROM reports
        WHERE status != 'DELETED'
        AND (:category IS NULL OR category = :category)
        AND (:status IS NULL OR status = :status)
        ORDER BY createdAt DESC
    """
    )
    fun getFilteredReports(category: String?, status: String?): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE userId = :userId AND status != 'DELETED' ORDER BY createdAt DESC")
    fun getReportsByUser(userId: String): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<ReportEntity>)

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Delete
    suspend fun deleteReport(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: String)

    @Query("SELECT COUNT(*) FROM reports WHERE status != 'DELETED'")
    fun getTotalReportsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reports WHERE status = :status")
    fun getReportsCountByStatus(status: String): Flow<Int>
}
