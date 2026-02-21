package com.dishut_lampung.sitanihut.domain.usecase.auth

import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await

class LogoutUseCase (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        try {
            val currentFcmToken = Firebase.messaging.token.await()
            authRepository.logout(currentFcmToken)
            Firebase.messaging.deleteToken().await()
        } catch (e: Exception) {
            e.printStackTrace()
            authRepository.logout("")
        }
    }
}