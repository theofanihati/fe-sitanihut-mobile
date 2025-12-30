package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.KthListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KthApiService {
    @GET("v1/kth")
    suspend fun getKthList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): ApiResponse<PaginatedData<KthListItemDto>>

    @DELETE("v1/kth/{id}")
    suspend fun deleteKth(
        @Path("id") id: String
    ): ApiResponse<Unit>
}