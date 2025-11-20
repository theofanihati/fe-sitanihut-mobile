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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
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
                val response = apiService.getUserDetail("Bearer $token", userId)

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
                flowOf(ReportSummary(0, 0, 0))
            } else {
                reportDao.getReportSummaryStat(userId).map { tuple ->
                    ReportSummary(
                        pendingCount = tuple.PENDING,
                        approvedCount = tuple.APPROVED,
                        rejectedCount = tuple.REJECTED
                    )
                }
            }
        }
    }

    override fun getLatestReports(): Flow<List<Report>> {
        val localData = reportDao.getLatestReports().map { entities ->
            entities.map { it.toDomain() }
        }

        return localData.onStart {
            fetchReportsFromNetwork()
        }
    }

    private suspend fun fetchReportsFromNetwork() {
        try {
            val token = userPreferences.getAuthToken()

            if (token != null) {
                val networkResponse = apiService.getLatestReports("Bearer $token")
                if (networkResponse.statusCode == 200) {
                    val reportDtos = networkResponse.data.items
                    val reportEntities = reportDtos.map { it.toEntity() }
                    reportDao.upsertAll(reportEntities)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override suspend fun deleteReport(reportId: String): Resource<Unit> {
        return try {
            reportDao.deleteReportById(reportId)

            val token = userPreferences.getAuthToken()
            if (token != null) {
                apiService.deleteReport("Bearer $token", reportId)
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
                apiService.submitReport("Bearer $token", reportId, methodPart, statusPart)
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gagal mengajukan laporan: ${e.message}")
        }
    }
}