package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ReviewReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(
        reportId: String,
        newStatus: ReportStatus,
        userRole: String
    ): Resource<Unit> {
        return TODO()
    }
}