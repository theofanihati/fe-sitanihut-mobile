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
import kotlinx.coroutines.flow.emitAll
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

        return localProfileFlow
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

                val remoteData = response.data.data
                if (remoteData.isNotEmpty()) {
//                    reportDao.upsertAll(remoteData.map { it.toEntity() })
                    val skeletons = remoteData.map { it.toEntity() }
                    reportDao.upsertPartialBatch(skeletons)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getReportsByStatus(status: String): Flow<Resource<List<Report>>> = flow {
        emit(Resource.Loading())
        val localData = reportDao.getReportsByStatus(status).first()

        if (localData.isNotEmpty()) {
            emit(Resource.Success(localData.map { it.toDomain() }))
        }

        try {
            val token = userPreferences.getAuthToken()
            if (token != null) {
                val response = apiService.getReportsByStatus(status)
                val remoteList = response.data.data ?: emptyList()
                if (remoteList.isNotEmpty()) {
//                    reportDao.upsertAll(remoteList.map { it.toEntity() })
                    val skeletons = remoteList.map { it.toEntity() }
                    reportDao.upsertPartialBatch(skeletons)
                }
            }
        } catch (e: Exception) {
            if (localData.isEmpty()) {
            }
        }

        emitAll(
            reportDao.getReportsByStatus(status).map { entities ->
                Resource.Success(entities.map { it.toDomain() })
            }
        )
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