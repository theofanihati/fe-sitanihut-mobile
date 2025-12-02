package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class DeleteReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(id: String): Resource<Unit> {
        TODO("belum beb")
    }
}