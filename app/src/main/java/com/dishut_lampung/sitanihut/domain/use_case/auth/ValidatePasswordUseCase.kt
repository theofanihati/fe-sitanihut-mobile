package com.dishut_lampung.sitanihut.domain.use_case.auth

import com.dishut_lampung.sitanihut.domain.validator.ValidationResult

class ValidatePasswordUseCase {
    operator fun invoke(password: String): ValidationResult {
        // TODO blumm
        return ValidationResult(false, errorMessage = "belumm")
    }
}