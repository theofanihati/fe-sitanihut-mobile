package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.PenyuluhDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.PenyuluhListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PenyuluhApiService {
    @GET("v1/penyuluh")
    suspend fun getPenyuluhList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null
    ): ApiResponse<PaginatedData<PenyuluhListItemDto>>

    @GET("v1/penyuluh/{id}")
    suspend fun getPenyuluhDetail(
        @Path("id") id: String
    ): ApiResponse<PenyuluhDetailDto>
}