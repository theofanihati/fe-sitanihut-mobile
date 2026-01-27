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
import com.dishut_lampung.sitanihut.domain.model.ReportAttachment
import com.dishut_lampung.sitanihut.util.getMimeType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
                SyncStatus.PENDING_REVIEW -> syncReview(report)
                else -> true
            }
            if (!success) isSuccess = false
        }
        return if (isSuccess) Result.success() else Result.retry()
    }

    private suspend fun syncCreate(report: ReportEntity): Boolean {
        if (report.jsonPayload == null) return false
        val parts = mutableListOf<MultipartBody.Part>()

        try {
            val dto = Gson().fromJson(report.jsonPayload, ReportRequestDto::class.java)
            fun addText(key: String, value: Any?) {
                if (value != null) {
                    val body = value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    parts.add(MultipartBody.Part.createFormData(key, null, body))
                }
            }

            addText("periode", dto.period)
            addText("bulan", dto.month?.lowercase())
            addText("modal", dto.modal?.toLong())
            addText("nte", dto.nte?.toLong())
            addText("status", dto.status)
            addText("catatan_petani", dto.farmerNotes ?: "")

            dto.plantingDetails?.forEachIndexed { i, item ->
                addText("masa_tanam[$i][tanggal]", item.date)
                addText("masa_tanam[$i][id_komoditas]", item.commodityId)
                addText("masa_tanam[$i][jumlah]", item.amount)
                addText("masa_tanam[$i][usia_tanam]", item.plantAge)
            }

            dto.harvestDetails?.forEachIndexed { i, item ->
                addText("masa_panen[$i][tanggal]", item.date)
                addText("masa_panen[$i][id_komoditas]", item.commodityId)
                addText("masa_panen[$i][jumlah]", item.amount)
                addText("masa_panen[$i][harga_satuan]", item.unitPrice)
            }

            val attachmentListType = object : TypeToken<List<ReportAttachment>>() {}.type
            val attachments: List<ReportAttachment> = Gson().fromJson(report.attachmentsJson, attachmentListType) ?: emptyList()

            attachments
                .filter { it.isLocal }
                .forEach { item ->
                    val file = File(item.filePath)
                    if (file.exists()) {
                        val mimeType = getMimeType(file)
                        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        parts.add(MultipartBody.Part.createFormData("lampiran[]", file.name, requestFile))
                    }
                }

            val response = apiService.createReport(parts)

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
                val errorCode = response.code()
                val errorBodyString = response.errorBody()?.string()

                Log.e("SYNC_WORKER", "Create Failed. Code: $errorCode")
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
            Log.e("SYNC_WORKER", "Exception: ${e.message}")
            return false
        }
    }

    private suspend fun syncUpdate(report: ReportEntity): Boolean {
        Log.d("SYNC_WORKER", "Updating report: ${report.id}")
        Log.d("SYNC_WORKER", "Payload JSON Content: ${report.jsonPayload}")

        if (report.jsonPayload == null) return false

        try {
            val dto = Gson().fromJson(report.jsonPayload, ReportRequestDto::class.java)
            val parts = mutableListOf<MultipartBody.Part>()

            fun addText(key: String, value: Any?) {
                if (value != null) {
                    val body = value.toString().toRequestBody(null)
                    parts.add(MultipartBody.Part.createFormData(key, null, body))
                }
            }

            addText("_method", "PATCH") // Spoofing
            addText("bulan", dto.month?.lowercase())
            addText("periode", dto.period)
            addText("modal", dto.modal?.toLong()) // Kirim integer string
            addText("nte", dto.nte?.toLong())
            addText("catatan_petani", dto.farmerNotes ?: "")
            addText("status", dto.status)

            dto.plantingDetails?.forEachIndexed { i, item ->
                addText("masa_tanam[$i][tanggal]", item.date)
                addText("masa_tanam[$i][id_komoditas]", item.commodityId)
                addText("masa_tanam[$i][jumlah]", item.amount)
                addText("masa_tanam[$i][usia_tanam]", item.plantAge)
            }
            dto.harvestDetails?.forEachIndexed { i, item ->
                addText("masa_panen[$i][tanggal]", item.date)
                addText("masa_panen[$i][id_komoditas]", item.commodityId)
                addText("masa_panen[$i][jumlah]", item.amount)
                addText("masa_panen[$i][harga_satuan]", item.unitPrice)
            }

            val attachmentListType = object : TypeToken<List<ReportAttachment>>() {}.type
            val attachments: List<ReportAttachment> = Gson().fromJson(report.attachmentsJson, attachmentListType) ?: emptyList()
            val existingIds = attachments.filter { !it.isLocal && !it.id.isNullOrEmpty() }.map { it.id!! }

            if (existingIds.isEmpty() && attachments.none { it.isLocal }) {
                addText("lampiran_existing[]", "")
            } else {
                existingIds.forEach { id ->
                    addText("lampiran_existing[]", id)
                }
            }
            attachments.filter { it.isLocal }.forEach { item ->
                val file = File(item.filePath)
                if (file.exists()) {
                    val requestFile = file.asRequestBody(getMimeType(file).toMediaTypeOrNull())
                    parts.add(MultipartBody.Part.createFormData("lampiran_new[]", file.name, requestFile))
                }
            }

//            report.attachmentPaths?.split(",")?.filter { it.isNotEmpty() }?.forEach { path ->
//                val file = File(path)
//                if (file.exists()) {
//                    val requestFile = file.asRequestBody(getMimeType(file).toMediaTypeOrNull())
//                    parts.add(MultipartBody.Part.createFormData("lampiran_new[]", file.name, requestFile))
//                }
//            }

            val response = apiService.updateReport(report.id, parts)
            if (response.isSuccessful) {
                val updated = report.copy(syncStatus = SyncStatus.SYNCED, jsonPayload = null)
                reportDao.upsertAll(listOf(updated))
                Log.d("SYNC_WORKER_SUCCESS", "Server Response: ${response.body()}")
                return true
            } else {
                Log.e("SYNC_WORKER", "Gagal Update: ${response.code()}")
                return false
            }
        }catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private suspend fun syncDelete(report: ReportEntity): Boolean {
        Log.d("SYNC_WORKER", "Deleting report: ${report.id}")
        try {
            val response = apiService.deleteReport(report.id)
            if (response.isSuccessful || response.code() == 404) {
                Log.d(
                    "SYNC_WORKER",
                    "Delete success/already deleted on server. Removing local data."
                )
                reportDao.deleteReportById(report.id)
                return true
            } else {
                Log.e("SYNC_WORKER", "Failed to delete. Code: ${response.code()}")
                return false
            }
        } catch (e: Exception) {
             e.printStackTrace()
             Log.e("SYNC_WORKER", "Exception during delete: ${e.message}")
             return false
        }
    }

    private suspend fun syncReview(report: ReportEntity): Boolean {
        Log.d("SYNC_WORKER", "Reviewing report status: ${report.id} to ${report.status}")
        try {
            val statusPart = report.status.toRequestBody("text/plain".toMediaTypeOrNull())
            val methodPart = "PATCH".toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.submitReport(
                id = report.id,
                method = methodPart,
                status = statusPart
            )

            if (response.statusCode == 200) {
                val updated = report.copy(syncStatus = SyncStatus.SYNCED)
                reportDao.upsertAll(listOf(updated))
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}