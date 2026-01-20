package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.RoleDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import retrofit2.http.GET
import retrofit2.http.Query

interface RoleApiService {
    @GET("v1/role")
    suspend fun getRoles(
        @Query("page") page: Int,
    ): ApiResponse<PaginatedData<RoleDto>>
    }