package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

class AuthDto {
    data class LoginRequest(
        val email: String,
        val password: String
    )

    data class LoginDataDto(
        val token: String,
        val id: String,
        val name: String,
        val role: String,
        val email: String
    )

    data class LoginResponse(
        val message: String,
        @SerializedName("statusCode")
        val statusCode: Int,
        val data: JsonElement
    )

    data class ForgotPasswordRequest(
        val email: String
    )

    data class GeneralResponse(
        val message: String,
        @SerializedName("statusCode")
        val statusCode: Int,
        val data: JsonElement?
    )
    data class ErrorResponse(
        @SerializedName("message")
        val message: String?
    )
}