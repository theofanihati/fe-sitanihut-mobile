package com.dishut_lampung.sitanihut.domain.usecase.report

import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.validator.ValidationResult
import javax.inject.Inject

class ValidateReportInputUseCase @Inject constructor() {
    fun execute(input: CreateReportInput): ValidationResult {
        return ValidationResult(false, errorMessage = "belumm")
    }
}