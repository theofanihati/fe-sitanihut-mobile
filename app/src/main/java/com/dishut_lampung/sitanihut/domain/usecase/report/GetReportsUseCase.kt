package com.dishut_lampung.sitanihut.domain.usecase.report

import androidx.paging.PagingData
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReportsUseCase @Inject constructor(
   private val reportRepository: ReportRepository
) {
    operator fun invoke(
        params: String = "",
        status: ReportStatus? = null
    ): Flow<PagingData<Report>> {
        return reportRepository.getReports(params, status)
    }
}