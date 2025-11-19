package com.dishut_lampung.sitanihut.domain.usecase.home

import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportSummary
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class FarmerHomeData(
    val userProfile: UserProfile,
    val summary: ReportSummary,
    val latestReports: List<Report>
)

class GetFarmerHomeDataUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    operator fun invoke(): Flow<FarmerHomeData> {
        return combine(
            homeRepository.getUserProfile(),
            homeRepository.getReportSummary(),
            homeRepository.getLatestReports()
        ) { profile, summary, reports ->
            FarmerHomeData(
                userProfile = profile,
                summary = summary,
                latestReports = reports
            )
        }
    }
}