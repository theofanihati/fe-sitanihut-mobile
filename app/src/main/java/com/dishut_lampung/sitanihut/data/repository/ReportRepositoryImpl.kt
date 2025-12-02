package com.dishut_lampung.sitanihut.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val apiService: ReportApiService,
    private val db: SitanihutDatabase,
    private val reportDao: ReportDao,
    private val userPreferences: UserPreferences
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
                status = status?.name
            ),
            pagingSourceFactory = {
                db.reportDao().getReports(query = params, status = status?.name)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun deleteReport(reportId: String): Resource<Unit> {
        return TODO("blum la")
    }

    override suspend fun submitReport(reportId: String): Resource<Unit> {
        return TODO("blum la")
    }
}