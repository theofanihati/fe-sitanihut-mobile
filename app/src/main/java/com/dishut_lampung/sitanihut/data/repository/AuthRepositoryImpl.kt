package com.dishut_lampung.sitanihut.data.repository

import retrofit2.HttpException
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.remote.AuthApiService
import com.dishut_lampung.sitanihut.data.remote.dto.AuthDto
import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.model.User
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val userPreferences: UserPreferences,
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun requestPasswordReset(email: String): AuthResult<Unit> {
        TODO("Not yet implemented")
    }
    override suspend fun logout() {
        TODO("Not yet implemented")

    }

    override suspend fun isLoggedIn(): Boolean {
        TODO("Not yet implemented")
    }
}
