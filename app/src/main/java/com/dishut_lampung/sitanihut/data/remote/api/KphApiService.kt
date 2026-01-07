package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.KphDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import retrofit2.http.GET
import retrofit2.http.Query

interface KphApiService {
    @GET("v1/kph")
    suspend fun getKphList(
        @Query("page") page: Int,
    ): ApiResponse<PaginatedData<KphDto>>
}