package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class CreateReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(input: CreateReportInput): Resource<Unit> {
        return repository.createReport(input)
    }
}