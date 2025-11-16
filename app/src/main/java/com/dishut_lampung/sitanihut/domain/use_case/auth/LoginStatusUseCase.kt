package com.dishut_lampung.sitanihut.domain.use_case.auth

import com.dishut_lampung.sitanihut.domain.repository.AuthRepository

class LoginStatusUseCase (
    private val authRepository: AuthRepository
    ) {
        suspend operator fun invoke(): Boolean  {
            return authRepository.isLoggedIn()
        }
    }