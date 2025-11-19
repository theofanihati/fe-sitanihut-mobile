package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportSummary
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

interface  HomeRepository {
    fun getUserProfile(): Flow<UserProfile>
    fun getReportSummary(): Flow<ReportSummary>
    fun getLatestReports(): Flow<List<Report>>
    suspend fun deleteReport(reportId: String): Resource<Unit>
    suspend fun submitReport(reportId: String): Resource<Unit>
    suspend fun syncUserProfile()
}