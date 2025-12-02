package com.dishut_lampung.sitanihut.domain.repository

import androidx.paging.PagingData
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getReports(params: String, status: ReportStatus?): Flow<PagingData<Report>>
    suspend fun deleteReport(reportId: String): Resource<Unit>
    suspend fun submitReport(reportId: String): Resource<Unit>
}