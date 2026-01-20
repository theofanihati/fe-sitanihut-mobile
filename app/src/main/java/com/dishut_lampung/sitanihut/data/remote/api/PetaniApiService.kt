package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.CreatePetaniRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.PetaniDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.PetaniListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface PetaniApiService {
    @GET("v1/petani")
    suspend fun getPetaniList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null
    ): ApiResponse<PaginatedData<PetaniListItemDto>>

    @GET("v1/petani/{id}")
    suspend fun getPetaniDetail(
        @Path("id") id: String
    ): ApiResponse<PetaniDetailDto>

    @POST("v1/petani")
    suspend fun createPetani(
        @Body request: CreatePetaniRequestDto
    ): ApiResponse<PetaniDetailDto>

    @PATCH("v1/petani/{id}")
    suspend fun updatePetani(
        @Path("id") id: String,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): ApiResponse<Unit>

    @DELETE("v1/petani/{id}")
    suspend fun deletePetani(
        @Path("id") id: String
    ): ApiResponse<Unit>

    // ============ KLO EXPORT BACK END ===================
//    @Streaming
//    @GET("petani/export-pdf")
//    suspend fun downloadLaporanPetani(
//        @Query("search") search: String = "",
//        @Query("kthId") kthId: String? = null
//    ): Response<ResponseBody>
//
//    @Streaming
//    @GET("petani/export-detail-pdf")
//    suspend fun downloadDetailPetani(
//        @Query("id") id: String
//    ): Response<ResponseBody>
}