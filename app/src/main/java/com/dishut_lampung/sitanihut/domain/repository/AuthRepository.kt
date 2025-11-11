package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.AuthResult

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult
}
