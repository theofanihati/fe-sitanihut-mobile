package com.dishut_lampung.sitanihut.domain.use_case

import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        // belum diimplementasi, biar test gagal dulu (fase RED)
        return AuthResult.Error("Belum diimplementasi")
    }
}