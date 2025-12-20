package com.dishut_lampung.sitanihut.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.repository.CommodityRepository
import com.dishut_lampung.sitanihut.domain.repository.PenyuluhRepository
import com.dishut_lampung.sitanihut.domain.repository.ProfileRepository
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val commodityRepository: CommodityRepository,
    private val profileRepository: ProfileRepository,
    private val reportRepository: ReportRepository,
    private val penyuluhRepository: PenyuluhRepository,
    private val userPreferences: UserPreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SYNC_WORKER_DATA", "Mulai sync user...")
            profileRepository.syncUserDetail()
            Log.d("SYNC_WORKER_DATA", "Mulai sync commodities...")
            commodityRepository.syncCommodities()
            Log.d("SYNC_WORKER_DATA", "Mulai sync reports...")
            reportRepository.syncReportDetail()
            Log.d("SYNC_WORKER_DATA", "Mulai sync penyuluh...")
            penyuluhRepository.syncPenyuluhData()
            Log.d("SYNC_WORKER", "Sync selesai. Update timestamp.")
            userPreferences.updateLastSyncTime()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}