package com.dishut_lampung.sitanihut.domain.use_case

import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        if (email.isBlank()) {
            return AuthResult.Error("Email tidak boleh kosong")
        }

        if (password.isBlank()) {
            return AuthResult.Error("Password tidak boleh kosong")
        }

        return authRepository.login(email, password)
    }
}