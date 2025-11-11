package com.dishut_lampung.sitanihut.domain.use_case

import android.util.Patterns
import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository

class ForgotPasswordUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): AuthResult<Unit> {
        // belum diimplementasi, biar test gagal dulu (fase RED)
        return AuthResult.Error<Unit>("Belum diimplementasi")
    }
}