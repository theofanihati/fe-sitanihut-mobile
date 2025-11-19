package com.dishut_lampung.sitanihut.domain.usecase.auth

import com.dishut_lampung.sitanihut.domain.repository.AuthRepository

class LogoutUseCase (
    private val authRepository: AuthRepository
    ) {
        suspend operator fun invoke() {
            authRepository.logout()
        }
}