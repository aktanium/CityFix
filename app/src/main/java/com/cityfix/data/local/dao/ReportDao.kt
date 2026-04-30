package com.cityfix.data.local.dao

import androidx.room.*
import com.cityfix.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id")
    fun getReportById(id: Long): Flow<ReportEntity?>

    @Query("SELECT * FROM reports WHERE category = :category ORDER BY createdAt DESC")
    fun getReportsByCategory(category: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status = :status ORDER BY createdAt DESC")
    fun getReportsByStatus(status: String): Flow<List<ReportEntity>>

    @Query(
        """
        SELECT * FROM reports 
        WHERE (:category IS NULL OR category = :category)
        AND (:status IS NULL OR status = :status)
        ORDER BY createdAt DESC
    """
    )
    fun getFilteredReports(category: String?, status: String?): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Delete
    suspend fun deleteReport(report: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("SELECT COUNT(*) FROM reports")
    fun getTotalReportsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reports WHERE status = :status")
    fun getReportsCountByStatus(status: String): Flow<Int>
}
