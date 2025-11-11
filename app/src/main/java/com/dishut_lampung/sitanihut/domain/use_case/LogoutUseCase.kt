package com.dishut_lampung.sitanihut.domain.use_case

import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository

class LogoutUseCase (
    private val authRepository: AuthRepository
    ) {
        suspend operator fun invoke(): AuthResult<Unit> {
            // belum diimplementasi, biar test gagal dulu (fase RED)
            return AuthResult.Error<Unit>("Belum diimplementasi")
        }
}