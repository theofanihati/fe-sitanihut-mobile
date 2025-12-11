package com.dishut_lampung.sitanihut.data.local.dao

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

data class ReportCountTuple(
    val PENDING: Int,
    val VERIFIED: Int,
    val APPROVED: Int,
    val REJECTED: Int,
)

@Dao
interface ReportDao {
    @Query("""
        SELECT * FROM laporan 
        WHERE (
            month LIKE '%' || :query || '%'
            OR period LIKE '%' || :query || '%'
            OR status LIKE '%' || :query || '%'
            OR nte LIKE '%' || :query || '%')
        AND (:status IS NULL OR status = :status)
        ORDER BY date DESC
    """)
    fun getReports(query: String, status: String?): PagingSource<Int, ReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<ReportEntity>)

    @Upsert
    suspend fun upsertAll(reports: List<ReportEntity>)

    @Transaction
    suspend fun insertOrIgnorePending(newReports: List<ReportEntity>) {
        newReports.forEach { report ->
            val localReport = getReportById(report.id)

            if (localReport == null) {
                upsertAll(listOf(report))
            } else {
                if (localReport.syncStatus == SyncStatus.SYNCED) {
                    upsertAll(listOf(report))
                }
            }
        }
    }

    @Query("SELECT * FROM laporan WHERE userId = :userId ORDER BY date DESC LIMIT 10")
    fun getLatestReports(userId: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM laporan WHERE status = :status ORDER BY date DESC")
    fun getReportsByStatus(status: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM laporan WHERE LOWER(syncStatus) = :status")
    suspend fun getReportsBySyncStatus(status: String): List<ReportEntity>

    @Query("""
    SELECT 
        COUNT(CASE WHEN LOWER(status) = 'menunggu' THEN 1 END) as PENDING, 
        COUNT(CASE WHEN LOWER(status) = 'diverifikasi' THEN 1 END) as VERIFIED, 
        COUNT(CASE WHEN LOWER(status) = 'disetujui' THEN 1 END) as APPROVED, 
        COUNT(CASE WHEN LOWER(status) = 'ditolak' THEN 1 END) as REJECTED
    FROM laporan
    WHERE userId = :userId
""")
    fun getReportSummaryStat(userId: String): Flow<ReportCountTuple>

    @Query("SELECT * FROM laporan WHERE id = :id")
    suspend fun getReportById(id: String): ReportEntity?

    @Query("SELECT * FROM laporan WHERE id = :id")
    fun getReportByIdFlow(id: String): Flow<ReportEntity?>

    @Query("DELETE FROM laporan WHERE id = :reportId")
    suspend fun deleteReportById(reportId: String)

    @Query("UPDATE laporan SET status = 'menunggu', syncStatus = 'pending_upload' WHERE id = :reportId")
    suspend fun submitReportById(reportId: String)

    @Query("DELETE FROM laporan WHERE syncStatus = 'SYNCED'")
    suspend fun clearSyncedReports()

    @Query("DELETE FROM laporan")
    fun clearAllLaporan()
}