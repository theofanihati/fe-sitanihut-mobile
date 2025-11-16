package com.dishut_lampung.sitanihut.domain.use_case.auth

import com.dishut_lampung.sitanihut.domain.validator.ValidationResult

class ValidateEmailUseCase {
    operator fun invoke(email: String): ValidationResult {
        // TODO blumm
        return ValidationResult(false, errorMessage = "belumm")
    }
}