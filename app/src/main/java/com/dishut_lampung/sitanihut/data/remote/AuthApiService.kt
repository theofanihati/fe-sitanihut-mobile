package com.dishut_lampung.sitanihut.data.remote

import com.dishut_lampung.sitanihut.data.remote.dto.AuthDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("/api/v1/login")
    suspend fun login(@Body request: AuthDto.LoginRequest): AuthDto.LoginResponse

    @POST("/api/v1/forgot-password")
    suspend fun requestPasswordReset(@Body request: AuthDto.ForgotPasswordRequest): AuthDto.GeneralResponse
}