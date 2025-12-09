package com.dishut_lampung.sitanihut.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.data.remote.dto.ConflictResponseDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportRequestDto
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@HiltWorker
class ReportSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiService: ReportApiService,
    private val reportDao: ReportDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pendingReports = reportDao.getReportsBySyncStatus("pending_create")
        if (pendingReports.isEmpty()) return Result.success()
        var isSuccess = true

        pendingReports.forEach { report ->
            try {
                if (report.jsonPayload == null) return@forEach
                val dataPart = report.jsonPayload.toRequestBody("application/json".toMediaTypeOrNull())

                val attachmentParts = report.attachmentPaths?.split(",")
                    ?.filter { it.isNotEmpty() }
                    ?.mapNotNull { path ->
                        val file = File(path)
                        if (file.exists()) {
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            MultipartBody.Part.createFormData("lampiran[]", file.name, requestFile)
                        } else null
                    } ?: emptyList()

                // HIT API
                val response = apiService.createReport(dataPart, attachmentParts)

                if (response.isSuccessful) {
                    val apiBody = response.body()
                    if (apiBody != null && (apiBody.statusCode == 200 || apiBody.statusCode == 201)) {
                        val updatedReport = report.copy(
                            syncStatus = SyncStatus.SYNCED,
                            jsonPayload = null
                        )
                        reportDao.upsertAll(listOf(updatedReport))
                    } else {
                        isSuccess = false
                    }
                } else {
                    // Handle Conflict ID. kesepakatan BE: BE generate UUID baru, client auto fetch & post
                    val errorCode = response.code()
                    val errorBodyString = response.errorBody()?.string()
                    Log.e("SYNC_DEBUG", "Error Code: $errorCode")
                    Log.e("SYNC_DEBUG", "Error Body: $errorBodyString")

                    if (errorCode == 409 && errorBodyString != null) {
                        try {
                            val conflictDto = Gson().fromJson(errorBodyString, ConflictResponseDto::class.java)
                            val newUuid = conflictDto.newUuid

                            val oldDto = Gson().fromJson(report.jsonPayload, ReportRequestDto::class.java)
                            val newDto = oldDto.copy(id = newUuid)
                            val newJsonPayload = Gson().toJson(newDto)
                            val newReportEntity = report.copy(
                                id = newUuid,
                                jsonPayload = newJsonPayload,
                                syncStatus = SyncStatus.PENDING_CREATE
                            )

                            // Ganti PK = Hapus & Insert, tiati
                            reportDao.deleteReportById(report.id)
                            reportDao.upsertAll(listOf(newReportEntity))
                            return Result.retry()

                        } catch (e: Exception) {
                            Log.e("SYNC_DEBUG", "Gagal parse conflict: ${e.message}")
                            e.printStackTrace()
                            isSuccess = false
                        }
                    } else {
                        isSuccess = false
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                isSuccess = false
            }
        }

        return if (isSuccess) Result.success() else Result.retry()
    }
}