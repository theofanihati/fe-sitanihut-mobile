package com.dishut_lampung.sitanihut.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReportSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiService: ReportApiService,
    private val reportDao: ReportDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return TODO()
    }
}