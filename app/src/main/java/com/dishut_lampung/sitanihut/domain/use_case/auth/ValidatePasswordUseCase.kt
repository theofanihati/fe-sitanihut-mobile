package com.dishut_lampung.sitanihut.domain.use_case.auth

import com.dishut_lampung.sitanihut.domain.validator.ValidationResult

class ValidatePasswordUseCase {

    private val kapitalRegex = Regex("[A-Z]")
    private val lowercaseRegex = Regex("[a-z]")
    private val numberRegex = Regex("[0-9]")
    private val specialCharRegex = Regex("[!@#\$%^&*(),.?\":{}|<>]")

    operator fun invoke(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password tidak boleh kosong"
            )
        }
        if (password.length < 8) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password minimal 8 karakter"
            )
        }
        if (!password.contains(kapitalRegex)) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password harus memiliki setidaknya satu huruf besar (kapital)"
            )
        }
        if (!password.contains(lowercaseRegex)) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password harus memiliki setidaknya satu huruf kecil"
            )
        }
        if (!password.contains(numberRegex)) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password harus memiliki setidaknya satu angka"
            )
        }
        if (!password.contains(specialCharRegex)) {
            return ValidationResult(
                successful = false,
                errorMessage = "Password harus memiliki setidaknya satu karakter spesial (!@#$...)"
            )
        }
        return ValidationResult(successful = true)
    }
}