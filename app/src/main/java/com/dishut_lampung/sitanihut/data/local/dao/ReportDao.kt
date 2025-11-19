package com.dishut_lampung.sitanihut.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

data class ReportCountTuple(
    val PENDING: Int,
    val APPROVED: Int,
    val REJECTED: Int
)

@Dao
interface ReportDao {
    @Upsert
    suspend fun upsertAll(reports: List<ReportEntity>)

    @Query("SELECT * FROM laporan ORDER BY date DESC LIMIT 10")
    fun getLatestReports(): Flow<List<ReportEntity>>

    @Query("""
    SELECT 
        COUNT(CASE WHEN status = 'menunggu' THEN 1 END) as PENDING, 
        COUNT(CASE WHEN status = 'disetujui' THEN 1 END) as APPROVED, 
        COUNT(CASE WHEN status = 'ditolak' THEN 1 END) as REJECTED
    FROM laporan
    WHERE id = :userId
""")
    fun getReportSummaryStat(userId: String): Flow<ReportCountTuple>

    @Query("DELETE FROM laporan WHERE id = :reportId")
    suspend fun deleteReportById(reportId: String)

    @Query("UPDATE laporan SET status = 'menunggu', syncStatus = 'pending_upload' WHERE id = :reportId")
    suspend fun submitReportById(reportId: String)

    @Query("DELETE FROM laporan")
    fun clearAllLaporan()
}