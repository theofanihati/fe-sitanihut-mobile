package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReportDetailUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    operator fun invoke(id: String): Flow<Resource<Report>> {
        return TODO()
    }
}