package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.RoleDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDetailDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UserApiService {
    @GET("v1/users/{id}")
    suspend fun getUserDetail(
        @Path("id") id: String
    ): ApiResponse<UserDetailDto>

    @GET("v1/roles")
    suspend fun getRoles(): ApiResponse<List<RoleDto>>
}