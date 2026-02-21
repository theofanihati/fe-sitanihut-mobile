package com.dishut_lampung.sitanihut.data.repository

import android.content.Context
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.data.mapper.toDbValue
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toDto
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.mapper.toReportDetail
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportRequestDto
import com.dishut_lampung.sitanihut.data.worker.ReportSyncWorker
import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.model.MasaPanen
import com.dishut_lampung.sitanihut.domain.model.MasaTanam
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportAttachment
import com.dishut_lampung.sitanihut.domain.model.ReportDetail
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import com.dishut_lampung.sitanihut.util.getCurrentDate
import com.dishut_lampung.sitanihut.util.getMimeType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val apiService: ReportApiService,
    private val db: SitanihutDatabase,
    private val reportDao: ReportDao,
    private val userPreferences: UserPreferences,
    @ApplicationContext private val context: Context
) : ReportRepository {

    private val gson = Gson()

    companion object {
        private const val WORK_QUEUE_NAME = "sync_report_queue"
        private const val TAG_REPORT_OP = "REPORT_OPERATION"
        private const val STATUS_WAITING = "menunggu"
        private const val STATUS_DRAFT = "belum diajukan"
        private const val OP_create = "CREATE"
        private const val OP_UPDATE = "UPDATE"
        private const val OP_DELETE = "DELETE"
        private const val OP_REVIEW = "REVIEW"
    }

    fun enqueueReportOperation(operationType: String, reportId: String) {
        val inputData = workDataOf(
            "OPERATION_TYPE" to operationType,
            "REPORT_ID" to reportId
        )

        val request = OneTimeWorkRequestBuilder<ReportSyncWorker>()
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setExpedited(androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(TAG_REPORT_OP)
            .addTag("REPORT_$operationType")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_QUEUE_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun getReports(params: String, status: ReportStatus?): Flow<PagingData<Report>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            remoteMediator = ReportRemoteMediator(
                apiService = apiService,
                db = db,
                query = params,
                status = status?.toDbValue()
            ),
            pagingSourceFactory = {
                val statusDbValue = status?.toDbValue()
                db.reportDao().getReports(query = params, status = statusDbValue)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun deleteReport(reportId: String): Resource<Unit> {
        return try {
            val currentStatus = reportDao.getSyncStatus(reportId)
            if (currentStatus == SyncStatus.PENDING_CREATE) {
                reportDao.deleteReportById(reportId)
            } else {
                reportDao.markAsPendingDelete(reportId)
                enqueueReportOperation(OP_DELETE, reportId)
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            handleException("Gagal menghapus data", e)
        }
    }

    override suspend fun submitReport(reportId: String): Resource<Unit> {
        return try {
            val oldReport = reportDao.getReportById(reportId)
                ?: return Resource.Error("Laporan tidak ditemukan di database lokal")

            val plantingList: List<MasaTanam> = Gson().fromJson(oldReport.plantingDetailsJson, object : TypeToken<List<MasaTanam>>() {}.type) ?: emptyList()
            val harvestList: List<MasaPanen> = Gson().fromJson(oldReport.harvestDetailsJson, object : TypeToken<List<MasaPanen>>() {}.type)?: emptyList()

            val requestDto = ReportRequestDto(
                id = reportId,
                updatedAt = getCurrentDate(),
                period = oldReport.period,
                month = oldReport.month,
                modal = oldReport.modal ?: 0.0,
                farmerNotes = oldReport.farmerNotes ?: "",
                nte = oldReport.nte,
                plantingDetails = plantingList.map { it.toDto() },
                harvestDetails = harvestList.map { it.toDto() },
                status = STATUS_WAITING
            )

            val updatedEntity = oldReport.copy(
                status = STATUS_WAITING,
                syncStatus = SyncStatus.PENDING_UPDATE,
                jsonPayload = gson.toJson(requestDto),
                date = getCurrentDate()
            )
            reportDao.upsertAll(listOf(updatedEntity))
            enqueueReportOperation(OP_UPDATE, reportId)

            Resource.Success(Unit)
        } catch (e: Exception) {
            handleException("Gagal mengajukan laporan", e)
        }
    }

    override suspend fun createReport(input: CreateReportInput): Resource<Unit>{
        return try {
            val reportId = UUID.randomUUID().toString()
            val currentUserId = userPreferences.userId.first() ?: ""

            val statusLaporan = if (input.isAjukan) "menunggu" else "belum diajukan"
            val newReport = buildNewReportEntity(
                id = reportId,
                userId = currentUserId,
                status = statusLaporan,
                input = input
            )
            reportDao.upsertAll(listOf(newReport))
            enqueueReportOperation(OP_create, reportId)


            Resource.Success(Unit)

        } catch (e: Exception) {
            handleException("Gagal menyimpan data", e)
        }
    }

    override fun getReportById(id: String): Flow<Resource<ReportDetail>> {
        return flow {
            emit(Resource.Loading())
            val localData = reportDao.getReportById(id)
            if (localData != null) {
                emit(Resource.Success(localData.toReportDetail()))
            }else {
                emit(Resource.Loading())
            }

            try {
                fetchAndSaveDetailFromNetwork(id)
            } catch (e: Exception) {
                Log.e("ReportRepo", "Background sync failed: ${e.message}")
            }

            reportDao.getReportByIdFlow(id).collect { entity ->
                if (entity != null) {
                    emit(Resource.Success(entity.toReportDetail()))
                } else {
                    if (localData == null) {
                        emit(Resource.Error("Laporan tidak ditemukan"))
                    }
                }
            }
        }
    }

    override suspend fun updateReport(id: String, input: CreateReportInput): Resource<Boolean> {
        return try {
            val oldReport = reportDao.getReportById(id)
                ?: return Resource.Error("Laporan tidak ditemukan")

            val statusLaporan = if (input.isAjukan) "menunggu" else "belum diajukan"

            val newSyncStatus = if (oldReport.syncStatus == SyncStatus.PENDING_CREATE) {
                SyncStatus.PENDING_CREATE
            } else {
                SyncStatus.PENDING_UPDATE
            }

            val jsonPayload = generateRequestJson(
                id = id,
                updatedAt = getCurrentDate(),
                input = input,
                status = statusLaporan
            )

            // room db update
            val updatedEntity = oldReport.copy(
                period = input.period,
                month = input.month,
                modal = input.modal.replace(".", "").toDoubleOrNull(),
                farmerNotes = input.farmerNotes,
                nte = input.nte,
                attachmentsJson = generateAttachmentsJson(input),
                status = statusLaporan,
                jsonPayload = jsonPayload,
                plantingDetailsJson = gson.toJson(input.plantingDetails),
                harvestDetailsJson = gson.toJson(input.harvestDetails),
                syncStatus = newSyncStatus,
                date = getCurrentDate()
            )
            reportDao.upsertAll(listOf(updatedEntity))
            enqueueReportOperation(OP_UPDATE, id)

            Resource.Success(true)
        } catch (e: Exception) {
            handleException("Gagal menyimpan perubahan", e)
        }
    }

    override suspend fun syncReportDetail(): Resource<Unit> {
        return try {
            val role = userPreferences.userRole.first() ?: "petani"
            val accumulatedDetailsToSave = mutableListOf<ReportEntity>()

            if (role.equals("petani", ignoreCase = true)) {
                val response = apiService.getLatestReports()
                val listData = response.data.data

                if (listData.isNotEmpty()) {
                    val targetItems = listData.take(10)
                    val details = fetchDetailsForList(targetItems)
                    accumulatedDetailsToSave.addAll(details)
                }

            } else {
                val targetStatuses = when {
                    role.equals("penyuluh", ignoreCase = true) -> listOf(
                        "menunggu" to 10,
                        "diverifikasi" to 5,
                        "disetujui" to 5,
                        "ditolak" to 2
                    )
                    role.equals("pj", ignoreCase = true) || role.contains("penanggung", ignoreCase = true) -> listOf(
                        "diverifikasi" to 10,
                        "disetujui" to 5,
                        "ditolak" to 2
                    )
                    else -> emptyList()
                }

                Log.d("ReportRepo", "Target status: $targetStatuses")

                targetStatuses.forEach { (status, limit) ->
                    try {
                        Log.d("ReportRepo", "Fetching reports status: $status")
                        val response = apiService.getReports(
                            page = 1,
                            limit = 10,
                            status = status
                        )
                        val listData = response.data.data

                        if (listData.isNotEmpty()) {
                            val targetItems = listData.take(limit)
                            val details = fetchDetailsForList(targetItems)
                            accumulatedDetailsToSave.addAll(details)

//                            reportDao.upsertAll(listData.map { it.toEntity() })
//                            val limitDetail = if (status == "menunggu" || status == "diverifikasi") 10 else 5
//
//                            listData.take(limitDetail).forEach { item ->
//                                fetchAndSaveDetailFromNetwork(item.id)
//                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ReportRepo", "Gagal fetch status $status: ${e.message}")
                    }
                }
            }
            if (accumulatedDetailsToSave.isNotEmpty()) {
                reportDao.upsertAll(accumulatedDetailsToSave)
                Log.d("ReportRepo", "Berhasil sinkronisasi ${accumulatedDetailsToSave.size} detail laporan")
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("ReportRepo", "Gagal sinkronisasi: ${e.message}", e)
            Resource.Error("Gagal sinkronisasi: ${e.message}")
        }
    }

    private suspend fun fetchDetailsForList(listData: List<ReportListItemDto>): List<ReportEntity> {
        val results = mutableListOf<ReportEntity>()

        listData.forEach { item ->
            try {
                val detailResponse = apiService.getReportDetail(item.id)

                if (detailResponse.statusCode == 200 && detailResponse.data != null) {
                    val networkEntity = detailResponse.data.toEntity()
                    val localData = reportDao.getReportById(item.id)

                    val finalEntity = if (localData != null) {
                        val isPending = localData.syncStatus != SyncStatus.SYNCED
                        if (isPending) {
                            localData
                        } else {
                            networkEntity.copy(
                                syncStatus = SyncStatus.SYNCED,
                                jsonPayload = localData.jsonPayload
                            )
                        }
                        networkEntity.copy(
                            syncStatus = if (localData.syncStatus != SyncStatus.SYNCED) localData.syncStatus else SyncStatus.SYNCED,
                            jsonPayload = localData.jsonPayload
                        )
                    } else {
                        networkEntity
                    }

                    results.add(finalEntity)
                }
            } catch (e: Exception) {
                Log.e("ReportRepo", "Gagal download detail ID ${item.id}: ${e.message}")
            }
        }
        return results
    }

    override suspend fun updateReportStatus(reportId: String, newStatus: ReportStatus): Resource<Unit> {
        return try {
            val oldReport = reportDao.getReportById(reportId)
                ?: return Resource.Error("Laporan tidak ditemukan di database lokal")

            val newSyncStatus = if (oldReport.syncStatus == SyncStatus.PENDING_CREATE) {
                SyncStatus.PENDING_CREATE
            } else {
                SyncStatus.PENDING_REVIEW
            }

            val updatedEntity = oldReport.copy(
                status = newStatus.toDbValue(),
                syncStatus = newSyncStatus,
                date = getCurrentDate()
            )

            reportDao.upsertAll(listOf(updatedEntity))
            if (newSyncStatus == SyncStatus.PENDING_REVIEW) {
                enqueueReportOperation(OP_REVIEW, reportId)
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            handleException("Gagal mengupdate status", e)
        }
    }

    private fun buildNewReportEntity(
        id: String,
        userId: String,
        status: String,
        input: CreateReportInput
    ): ReportEntity {
        val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        val localDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val jsonPayload = generateRequestJson(id, isoDate, input, status)

        return ReportEntity(
            id = id,
            userId = userId,
            period = input.period,
            month = input.month,
            date = localDate,
            nte = input.nte,
            status = status,
            modal = input.modal.replace(".", "").toDoubleOrNull(),
            farmerNotes = input.farmerNotes,
            plantingDetailsJson = gson.toJson(input.plantingDetails),
            harvestDetailsJson = gson.toJson(input.harvestDetails),
            syncStatus = SyncStatus.PENDING_CREATE,
            jsonPayload = jsonPayload,
            attachmentsJson = generateAttachmentsJson(input)
        )
    }

    private fun generateRequestJson(
        id: String,
        updatedAt: String,
        input: CreateReportInput,
        status: String
    ): String {
        val requestDto = input.toDto(id = id, updatedAt = updatedAt).copy(status = status)
        return gson.toJson(requestDto)
    }

    private fun generateAttachmentsJson(input: CreateReportInput): String {
        val attachments = mutableListOf<ReportAttachment>()
        input.existingAttachmentIds.forEach { id ->
            attachments.add(ReportAttachment(id = id, filePath = "", isLocal = false))
        }
        input.newAttachments.forEach { path ->
            attachments.add(ReportAttachment(id = null, filePath = path, isLocal = true))
        }
        return gson.toJson(attachments)
    }

    private suspend fun fetchAndSaveDetailFromNetwork(id: String) {
        try {
            val response = apiService.getReportDetail(id)
            if (response.statusCode == 200 && response.data != null) {
                val localReport = reportDao.getReportById(id)
                val isLocalPending = localReport?.syncStatus == SyncStatus.PENDING_UPDATE ||
                        localReport?.syncStatus == SyncStatus.PENDING_CREATE ||
                        localReport?.syncStatus == SyncStatus.PENDING_REVIEW

                if (isLocalPending) {
                    Log.d("ReportRepo", "Skip saving network data for ID: $id because local data is PENDING sync.")
                    return
                }

                reportDao.upsertAll(listOf(response.data.toEntity()))
                Log.d("ReportRepo", "Detail synced for ID: $id")
            }
        } catch (e: Exception) {
            Log.e("ReportRepo", "Failed to sync detail: ${e.message}")
        }
    }

    private fun <T> handleException(msg: String, e: Exception): Resource<T> {
        Log.e("ReportRepo", "$msg: ${e.message}", e)
        return Resource.Error("$msg: ${e.localizedMessage}")
    }
}