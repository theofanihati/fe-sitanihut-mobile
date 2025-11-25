package com.dishut_lampung.sitanihut.data.repository

import android.util.Log
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.HomeApiService
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportSummary
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val apiService: HomeApiService,
    private val reportDao: ReportDao,
    private val userPreferences: UserPreferences
) : HomeRepository {

    override fun getUserProfile(): Flow<UserProfile> {
        val localProfileFlow = combine(
            userPreferences.userName,
            userPreferences.userRole,
            userPreferences.userAvatar
        ) { name, role, avatar ->
            UserProfile(
                name = name ?: "Pengguna",
                role = role ?: "Role",
                profilePictureUrl = avatar
            )
        }

        return localProfileFlow.onStart {
            syncUserProfile()
        }
    }

    override suspend fun syncUserProfile() {
        try {
            val token = userPreferences.authToken.first()
            val userId = userPreferences.userId.first()
            Log.d("SITANIHUT_SYNC_AUTH", "Token: $token, User ID: $userId")
            Log.d("SITANIHUT_SYNC", "Token: $token, User ID: $userId")

            if (!token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                val response = apiService.getUserDetail(userId)

                if (response.statusCode == 200 && response.data != null) {
                    val data = response.data
                    Log.d("SITANIHUT_DATA", "API URL Avatar: ${data.profilePictureUrl}")
                    if (!data.profilePictureUrl.isNullOrEmpty()) {
                        userPreferences.saveUserAvatar(data.profilePictureUrl)
                    }

                    userPreferences.saveUserName(data.name)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getReportSummary(): Flow<ReportSummary> {
        return userPreferences.userId.flatMapLatest { userId ->
            if (userId.isNullOrEmpty()) {
                flowOf(ReportSummary(0, 0, 0,0))
            } else {
                reportDao.getReportSummaryStat(userId).map { tuple ->
                    ReportSummary(
                        pendingCount = tuple.PENDING,
                        verifiedcount = tuple.VERIFIED,
                        approvedCount = tuple.APPROVED,
                        rejectedCount = tuple.REJECTED
                    )
                }
            }
        }
    }

    override fun getLatestReports(): Flow<List<Report>> {
        return userPreferences.userId.flatMapLatest { userId ->
            if (userId.isNullOrEmpty()) {
                flowOf(emptyList())
            } else {
                reportDao.getLatestReports(userId).map { entities ->
                    entities.map { it.toDomain() }
                }.onStart {
                    syncMyReportHistory()
                }
            }
        }
    }

    private suspend fun syncMyReportHistory() {
        try {
            val token = userPreferences.getAuthToken()
            if (token != null) {
                val response = apiService.getLatestReports()

                val remoteData = response.data.items
                if (remoteData.isNotEmpty()) {
                    reportDao.upsertAll(remoteData.map { it.toEntity() })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getReportsByStatus(status: String): Flow<Resource<List<Report>>> = flow {
        emit(Resource.Loading())

        val localData = reportDao.getReportsByStatus(status).first()
        val localDomain = localData.map { it.toDomain() }

        if (localDomain.isNotEmpty()) {
            emit(Resource.Success(localDomain))
        }

        try {
            val token = userPreferences.getAuthToken()
            if (token.isNullOrBlank()) {
                if (localDomain.isEmpty()) emit(Resource.Error("Token expired"))
                return@flow
            }

            val response = apiService.getReportsByStatus(status)
            val remoteItems = response.data

            if (remoteItems != null) {
                reportDao.upsertAll(remoteItems.map { it.toEntity() })

                val newDomainData = remoteItems.map { it.toDomain() }
                emit(Resource.Success(newDomainData))
            } else if (localDomain.isEmpty()) {
                emit(Resource.Success(emptyList()))
            }

        } catch (e: IOException) {
            if (localDomain.isNotEmpty()) {
                emit(Resource.Success(localDomain))
            } else {
                emit(Resource.Error("Gagal terhubung internet."))
            }
        } catch (e: Exception) {
            if (localDomain.isEmpty()) emit(Resource.Error(e.localizedMessage ?: "Error"))
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
            Resource.Error("Gagal menghapus data: ${e.message}")
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
            Resource.Error("Gagal mengajukan laporan: ${e.message}")
        }
    }
}