package com.dishut_lampung.sitanihut.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.data.remote.dto.ConflictResponseDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportRequestDto
import com.dishut_lampung.sitanihut.util.getMimeType
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
        val unsyncedReports = reportDao.getAllUnsyncedReports()

        if (unsyncedReports.isEmpty()) return Result.success()
        var isSuccess = true

        unsyncedReports.forEach { report ->
            val success = when (report.syncStatus) {
                SyncStatus.PENDING_CREATE -> syncCreate(report)
                SyncStatus.PENDING_UPDATE -> syncUpdate(report)
                SyncStatus.PENDING_DELETE -> syncDelete(report)
                else -> true
            }
            if (!success) isSuccess = false
        }
        return if (isSuccess) Result.success() else Result.retry()
    }

    private suspend fun syncCreate(report: ReportEntity): Boolean {
        if (report.jsonPayload == null) return false
        val dataPart = report.jsonPayload.toRequestBody("application/json".toMediaTypeOrNull())

        val attachmentParts = report.attachmentPaths?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.mapNotNull { path ->
                val file = File(path)
                if (file.exists()) {
                    val mimeType = getMimeType(file)
                    val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("lampiran[]", file.name, requestFile)
                } else null
            } ?: emptyList()

            try {
                val response = apiService.createReport(
                    dataPart,
                    attachmentParts
                )

                if (response.isSuccessful) {
                    val apiBody = response.body()
                    if (apiBody != null && (apiBody.statusCode == 200 || apiBody.statusCode == 201)) {
                        val updatedReport = report.copy(
                            syncStatus = SyncStatus.SYNCED,
                            jsonPayload = null
                        )
                        reportDao.upsertAll(listOf(updatedReport))
                        return true
                    } else {
                        return false
                    }
                } else {
                    // Handle Conflict ID. kesepakatan BE: BE generate UUID baru, client auto fetch & post
                    val errorCode = response.code()
                    val errorBodyString = response.errorBody()?.string()
                    Log.e("SYNC_WORKER", "Error Code: $errorCode")
                    Log.e("SYNC_WORKER", "Error Body: $errorBodyString")

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
                            reportDao.upsertAll(listOf(newReportEntity))
                            reportDao.deleteReportById(report.id)
                            return false
                        } catch (e: Exception) {
                            Log.e("SYNC_WORKER", "Gagal parse conflict: ${e.message}")
                            e.printStackTrace()
                            return false
                        }
                    } else {
                        return false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
    }

    private suspend fun syncUpdate(report: ReportEntity): Boolean {
        Log.d("SYNC_WORKER", "Updating report: ${report.id}")
        Log.d("SYNC_WORKER", "Payload JSON Content: ${report.jsonPayload}")

        if (report.jsonPayload == null) return false
        val reportDto = Gson().fromJson(report.jsonPayload, ReportRequestDto::class.java)
        val partMap = HashMap<String, RequestBody>()
        val mediaType = "text/plain".toMediaTypeOrNull()

        partMap["periode"] = reportDto.period.toString().toRequestBody(mediaType)
        partMap["bulan"] = (reportDto.month ?: "").toRequestBody(mediaType)
        partMap["modal"] = (reportDto.modal ?: 0.0).toString().toRequestBody(mediaType)
        partMap["nte"] = (reportDto.nte ?: 0.0).toString().toRequestBody(mediaType)
        partMap["catatan_petani"] = (reportDto.farmerNotes ?: "").toRequestBody(mediaType)
        partMap["status"] = (reportDto.status ?: "menunggu").toRequestBody(mediaType)

        reportDto.plantingDetails?.forEachIndexed { index, item ->
            item.date?.let { partMap["masa_tanam[$index][tanggal]"] = it.toRequestBody(mediaType) }
            item.commodityId?.let { partMap["masa_tanam[$index][id_komoditas]"] = it.toRequestBody(mediaType) }
            partMap["masa_tanam[$index][usia_tanam]"] = (item.plantAge ?: 0.0).toString().toRequestBody(mediaType)
            partMap["masa_tanam[$index][jumlah]"] = (item.amount ?: 0.0).toString().toRequestBody(mediaType)
        }

        reportDto.harvestDetails?.forEachIndexed { index, item ->
            item.date?.let { partMap["masa_panen[$index][tanggal]"] = it.toRequestBody(mediaType) }
            item.commodityId?.let { partMap["masa_panen[$index][id_komoditas]"] = it.toRequestBody(mediaType) }
            partMap["masa_panen[$index][jumlah]"] = (item.amount ?: 0.0).toString().toRequestBody(mediaType)
            partMap["masa_panen[$index][harga_satuan]"] = (item.unitPrice ?: 0.0).toString().toRequestBody(mediaType)
        }

        val attachmentParts = report.attachmentPaths?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.mapNotNull { path ->
                val file = File(path)
                if (file.exists()) {
                    val mimeType = getMimeType(file)
                    val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("lampiran[]", file.name, requestFile)
                } else null
            } ?: emptyList()

        val methodPart = "PATCH".toRequestBody(mediaType)

        try {
            val response = apiService.updateReport(
                id = report.id,
                method = methodPart,
                data = partMap,
                attachments = attachmentParts
            )

            if (response.isSuccessful) {
                Log.d("SYNC_WORKER_SUCCESS", "Server Response: ${response.body()}")
                val updatedReport = report.copy(
                    syncStatus = SyncStatus.SYNCED,
                    jsonPayload = null
                )
                reportDao.upsertAll(listOf(updatedReport))
                return true
            } else {
                Log.e("SYNC_WORKER_ERROR", "Update failed: ${response.code()} ${response.errorBody()?.string()}")
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private suspend fun syncDelete(report: ReportEntity): Boolean {
        Log.d("SYNC_WORKER", "Deleting report: ${report.id}")
        return TODO("apiService.deleteReport(report.id)")
    }
}