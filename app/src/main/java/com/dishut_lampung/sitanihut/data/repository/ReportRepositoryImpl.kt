package com.dishut_lampung.sitanihut.data.repository

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.data.mapper.toCreateReportInput
import com.dishut_lampung.sitanihut.data.mapper.toDbValue
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toDto
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.data.remote.dto.ReportRequestDto
import com.dishut_lampung.sitanihut.data.worker.ReportSyncWorker
import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import com.dishut_lampung.sitanihut.util.getCurrentDate
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
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
            reportDao.deleteReportById(reportId)

            val token = userPreferences.getAuthToken()
            if (token != null) {
                apiService.deleteReport(reportId)
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Success(Unit)
        }
    }

    override suspend fun submitReport(reportId: String): Resource<Unit> {
        return try {
            reportDao.submitReportById(reportId)
            val token = userPreferences.getAuthToken()
            val methodPart = "PATCH".toRequestBody("text/plain".toMediaTypeOrNull())
            val statusPart = "menunggu".toRequestBody("text/plain".toMediaTypeOrNull())

            if (token != null) {
                apiService.submitReport(reportId, methodPart, statusPart)
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Success(Unit)
        }
    }

    override suspend fun createReport(input: CreateReportInput): Resource<Unit>{
        val reportId = UUID.randomUUID().toString()
        val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        val requestDto = input.toDto(id = reportId, updatedAt = isoDate)
        val gson = Gson()
        val jsonPayloadString = gson.toJson(requestDto)

        val filePathsString = input.attachments.joinToString(",")
        val currentUserId = userPreferences.userId.first() ?: ""

        val newReport = ReportEntity(
            id = reportId,
            userId = currentUserId,

            period = input.period,
            month = input.month,
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            nte = input.nte,
            status = "menunggu",

            syncStatus = SyncStatus.PENDING_CREATE,

            jsonPayload = jsonPayloadString,
            attachmentPaths = filePathsString
        )
        return try {
            reportDao.insertAll(listOf(newReport))

            val workRequest = OneTimeWorkRequestBuilder<ReportSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)

            Resource.Success(Unit)

        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gagal menyimpan data: ${e.message}")
        }
    }

    override fun getReportById(id: String): Flow<Resource<CreateReportInput>> {
        return TODO()
    }

    override suspend fun updateReport(id: String, input: CreateReportInput): Resource<Boolean> {
        return TODO()
    }
}