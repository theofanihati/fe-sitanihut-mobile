package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.CreateKthRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.KthDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.KthListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface KthApiService {
    @GET("v1/kth")
    suspend fun getKthList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): ApiResponse<PaginatedData<KthListItemDto>>

    @GET("v1/kth/{id}")
    suspend fun getKthDetail(
        @Path("id") id: String
    ): ApiResponse<KthDetailDto>

    @POST("v1/kth")
    suspend fun createKth(
        @Body request: CreateKthRequestDto
    ): ApiResponse<KthDetailDto>

    @PATCH("v1/kth/{id}")
    suspend fun updateKth(
        @Path("id") id: String,
        @Body request: CreateKthRequestDto
    ): ApiResponse<Unit>

    @DELETE("v1/kth/{id}")
    suspend fun deleteKth(
        @Path("id") id: String
    ): ApiResponse<Unit>
}