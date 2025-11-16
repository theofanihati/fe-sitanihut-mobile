package com.dishut_lampung.sitanihut.domain.use_case.auth

import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.model.User
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult<User> {
        if (email.isBlank()) {
            return AuthResult.Error<User>("Email tidak boleh kosong")
        }

        if (password.isBlank()) {
            return AuthResult.Error<User>("Password tidak boleh kosong")
        }

        return authRepository.login(email, password)
    }
}