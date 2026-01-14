package com.dishut_lampung.sitanihut.domain.usecase.auth

import com.dishut_lampung.sitanihut.domain.validator.ValidationResult
import java.util.regex.Pattern
import javax.inject.Inject

class ValidateLoginInputUseCase @Inject constructor() {
    operator fun invoke(input: String): ValidationResult {
        if (input.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Email / NIP / NIK tidak boleh kosong"
            )
        }

        if (input.all { it.isDigit() }) {
            val length = input.length
            return if (length == 16 || length == 18) {
                ValidationResult(successful = true)
            } else {
                ValidationResult(
                    successful = false,
                    errorMessage = "NIP/NIK berisi 16 atau 18 digit"
                )
            }
        }

        if (input.contains("@")) {
            val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
            )
            return if (EMAIL_ADDRESS_PATTERN.matcher(input).matches())  {
                ValidationResult(successful = true)
            } else {
                ValidationResult(
                    successful = false,
                    errorMessage = "Format email tidak valid"
                )
            }
        }
        return ValidationResult(
            successful = false,
            errorMessage = "Format tidak dikenali (Gunakan Email atau NIP/NIK)"
        )
    }
}