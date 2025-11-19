package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.HomeApiService
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
        return flowOf(
            UserProfile(
                name = "",
                role = "",
                profilePictureUrl = null
            )
        )
    }

    override fun getReportSummary(): Flow<ReportSummary> {
        return flowOf(ReportSummary(0, 0, 0))
    }

    override fun getLatestReports(): Flow<List<Report>> {
        return flowOf(emptyList())
    }

    override suspend fun deleteReport(reportId: String): Resource<Unit> {
        return Resource.Error("Fitur delete belum diimplementasi")
    }

    override suspend fun submitReport(reportId: String): Resource<Unit> {
        return Resource.Error("Fitur submit belum diimplementasi")
    }
}
