package com.dishut_lampung.sitanihut.domain.usecase.auth

import java.util.regex.Pattern
import com.dishut_lampung.sitanihut.domain.validator.ValidationResult

class ValidateEmailUseCase {
    companion object {
        private val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
    }
    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Email tidak boleh kosong"
            )
        }
        if (!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Format email tidak valid"
            )
        }
        return ValidationResult(successful = true)
    }
}