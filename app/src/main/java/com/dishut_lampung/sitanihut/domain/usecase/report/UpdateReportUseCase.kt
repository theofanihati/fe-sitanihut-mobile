package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.model.ReportDetail
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class UpdateReportUseCase @Inject constructor(
    private val repository: ReportRepository,
    private val validator: ValidateReportInputUseCase
) {
    suspend operator fun invoke(id: String, input: CreateReportInput): Resource<Boolean> {
        val validationResult = validator.execute(input)

        if (!validationResult.successful) {
            return Resource.Error(validationResult.errorMessage ?: "Mohon periksa inputan anda")
        }

        return repository.updateReport(id, input)
    }
}