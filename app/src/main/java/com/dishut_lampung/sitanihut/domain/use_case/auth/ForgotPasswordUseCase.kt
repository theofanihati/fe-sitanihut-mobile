package com.dishut_lampung.sitanihut.domain.use_case.auth

import java.util.regex.Pattern
import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository

class ForgotPasswordUseCase (
    private val authRepository: AuthRepository
) {
    private val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    suspend operator fun invoke(email: String): AuthResult<Unit> {
        if (email.isBlank()) {
            return AuthResult.Error("Email tidak boleh kosong")
        }

        if (!isEmailValid(email)) {
            return AuthResult.Error("Format email tidak valid")
        }

        return authRepository.requestPasswordReset(email)
    }

    private fun isEmailValid(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }
}