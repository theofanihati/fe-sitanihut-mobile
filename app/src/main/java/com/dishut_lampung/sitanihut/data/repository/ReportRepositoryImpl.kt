package com.dishut_lampung.sitanihut.data.repository

import android.content.Context
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
            .addTag("REPORT_OPERATION")
            .addTag("REPORT_$operationType")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync_report_queue",
            ExistingWorkPolicy.APPEND,
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
                enqueueReportOperation("DELETE", reportId)
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gagal menghapus data: ${e.message}")
        }
    }

    override suspend fun submitReport(reportId: String): Resource<Unit> {
        return try {
            val oldReport = reportDao.getReportById(reportId)
                ?: return Resource.Error("Laporan tidak ditemukan di database lokal")

            val newStatusString = "menunggu"

            val plantingType = object : TypeToken<List<MasaTanam>>() {}.type
            val harvestType = object : TypeToken<List<MasaPanen>>() {}.type

            val plantingList: List<MasaTanam> = Gson().fromJson(oldReport.plantingDetailsJson, plantingType) ?: emptyList()
            val harvestList: List<MasaPanen> = Gson().fromJson(oldReport.harvestDetailsJson, harvestType) ?: emptyList()

            val requestDto = ReportRequestDto(
                id = reportId,
                updatedAt = getCurrentDate(),
                period = oldReport.period,
                month = oldReport.month,
                modal = oldReport.modal ?: 0.0,
                farmerNotes = oldReport.farmerNotes?: "",
                nte = oldReport.nte,
                plantingDetails = plantingList.map { it.toDto() },
                harvestDetails = harvestList.map { it.toDto() },
                status = newStatusString
            )

            val jsonPayload = Gson().toJson(requestDto)

            val updatedEntity = oldReport.copy(
                status = newStatusString,
                syncStatus = SyncStatus.PENDING_UPDATE,
                jsonPayload = jsonPayload,
                date = getCurrentDate()
            )
            reportDao.upsertAll(listOf(updatedEntity))
            enqueueReportOperation("UPDATE", reportId)

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gagal mengajukan laporan: ${e.message}")
        }
    }

    override suspend fun createReport(input: CreateReportInput): Resource<Unit>{
        val reportId = UUID.randomUUID().toString()
        val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
        val localDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val statusLaporan = if (input.isAjukan) "menunggu" else "belum diajukan"
        val requestDto = input.toDto(
            id = reportId,
            updatedAt = isoDate
        ).copy(
            status = statusLaporan
        )
        val gson = Gson()
        val jsonPayloadString = gson.toJson(requestDto)
        val plantingJsonLocal = gson.toJson(input.plantingDetails)
        val harvestJsonLocal = gson.toJson(input.harvestDetails)

        val attachmentObjects = input.newAttachments.map { path ->
            ReportAttachment(
                id = null,
                filePath = path,
                isLocal = true
            )
        }
        val attachmentsJsonString = gson.toJson(attachmentObjects)

        val currentUserId = userPreferences.userId.first() ?: ""

        val newReport = ReportEntity(
            id = reportId,
            userId = currentUserId,

            period = input.period,
            month = input.month,
            date = localDate,
            nte = input.nte,
            status = statusLaporan,

            modal = input.modal.replace(".", "").toDoubleOrNull(),
            farmerNotes = input.farmerNotes,
            plantingDetailsJson = plantingJsonLocal,
            harvestDetailsJson = harvestJsonLocal,

            syncStatus = SyncStatus.PENDING_CREATE,

            jsonPayload = jsonPayloadString,
            attachmentsJson = attachmentsJsonString
        )
        return try {
            reportDao.upsertAll(listOf(newReport))
            enqueueReportOperation("CREATE", reportId)

            Resource.Success(Unit)

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gagal menyimpan data: ${e.message}")
        }
    }

    override fun getReportById(id: String): Flow<Resource<ReportDetail>> {
        return flow {
            emit(Resource.Loading())
            try {
                android.util.Log.d("REPO_DEBUG", "Fetching Detail ID: $id")
                val response = apiService.getReportDetail(id)
                if (response.statusCode == 200 && response.data != null) {
                    val detailDto = response.data
                    val entity = detailDto.toEntity()
                    reportDao.upsertAll(listOf(entity))
                    android.util.Log.d("DEBUG_EDIT_REPO", "Berhasil fetch detail. Tanam: ${entity.plantingDetailsJson}")
                }else {
                    android.util.Log.e("DEBUG_EDIT_REPO", "Gagal fetch detail: ${response.message}")
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("DEBUG_EDIT_REPO", "Server Error 500 Body: $errorBody")
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("DEBUG_EDIT_REPO", "Error network: ${e.message}")
            }

            reportDao.getReportByIdFlow(id).collect { entity ->
                if (entity != null) {
                    emit(Resource.Success(entity.toReportDetail()))
                } else {
                    emit(Resource.Error("Laporan tidak ditemukan"))
                }
            }
        }
    }

    override suspend fun updateReport(id: String, input: CreateReportInput): Resource<Boolean> {
        return try {
            val oldReport = reportDao.getReportById(id)
                ?: return Resource.Error("Laporan tidak ditemukan")

            val statusLaporan = if (input.isAjukan) "menunggu" else "belum diajukan"

            val newStatus = when (oldReport.syncStatus) {
                SyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
                else -> SyncStatus.PENDING_UPDATE
            }

            val requestDto = ReportRequestDto(
                id = id,
                updatedAt = getCurrentDate(),
                period = input.period,
                month = input.month,
                modal = input.modal.replace(".", "").toDoubleOrNull() ?: 0.0,
                farmerNotes = input.farmerNotes,
                nte = input.nte,
                plantingDetails = input.plantingDetails.map { it.toDto() },
                harvestDetails = input.harvestDetails.map { it.toDto() },
                status = statusLaporan

                )
            val jsonPayload = Gson().toJson(requestDto)

            val plantingJsonForEntity = Gson().toJson(input.plantingDetails)
            val harvestJsonForEntity = Gson().toJson(input.harvestDetails)


            val attachmentsForEntity = mutableListOf<ReportAttachment>()
            input.existingAttachmentIds.forEach { id ->
                attachmentsForEntity.add(ReportAttachment(id = id, filePath = "", isLocal = false))
            }
            input.newAttachments.forEach { path ->
                attachmentsForEntity.add(ReportAttachment(id = null, filePath = path, isLocal = true))
            }
            val attachmentsJsonString = Gson().toJson(attachmentsForEntity)

            // room db update
            val updatedEntity = oldReport.copy(
                period = input.period,
                month = input.month,
                modal = input.modal.replace(".", "").toDoubleOrNull(),
                farmerNotes = input.farmerNotes,
                nte = input.nte,
                attachmentsJson = attachmentsJsonString,
                status = statusLaporan,
                jsonPayload = jsonPayload,
                plantingDetailsJson = plantingJsonForEntity,
                harvestDetailsJson = harvestJsonForEntity,
                syncStatus = newStatus,
                date = getCurrentDate()
            )
            reportDao.upsertAll(listOf(updatedEntity))
            enqueueReportOperation("UPDATE", id)

            Resource.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gagal menyimpan perubahan: ${e.localizedMessage}")
        }
    }

    override suspend fun syncReportDetail(): Resource<Unit> {
        return try {
            val response = apiService.getLatestReports()
            val listData = response.data.data

            if (listData.isNotEmpty()) {
                reportDao.upsertAll(listData.map { it.toEntity() })
                val top10 = listData.take(10)

                top10.forEach { summaryItem ->
                    try {
                        val detailResponse = apiService.getReportDetail(summaryItem.id)
                        val detailDto = detailResponse.data
                        reportDao.upsertReport(detailDto.toEntity())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Gagal sinkronisasi: ${e.message}")
        }
    }
}