package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult<User>
    suspend fun requestPasswordReset(email: String): AuthResult<Unit>
}
