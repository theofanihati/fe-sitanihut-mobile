package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDto
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface HomeApiService {
    @GET("v1/laporan")
    suspend fun getLatestReports(
        @Query("limit") limit: Int = 10
    ): ApiResponse<PaginatedData<ReportListItemDto>>

    @GET("v1/laporan")
    suspend fun getReportsByStatus(
        @Query("status") status: String
    ): ApiResponse<PaginatedData<ReportListItemDto>>

    @DELETE("v1/laporan/{id}")
    suspend fun deleteReport(
        @Path("id") id: String
    ): ApiResponse<Any?>

    @Multipart
    @POST("v1/laporan/{id}")
    suspend fun submitReport(
        @Path("id") id: String,
        @Part("_method") method: RequestBody, // "PATCH"
        @Part("status") status: RequestBody   // "menunggu"
    ): ApiResponse<Any?>

    @GET("v1/users/{id}")
    suspend fun getUserDetail(
        @Path("id") id: String
    ): ApiResponse<UserDto>
}