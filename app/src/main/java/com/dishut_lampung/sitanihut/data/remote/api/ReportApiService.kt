package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.AuthDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportApiService {
    @GET("v1/laporan")
    suspend fun getReports(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
    ): ApiResponse<PaginatedData<ReportListItemDto>>

    @GET("v1/laporan/{id}")
    suspend fun getReportDetail(
        @Path("id") id: String
    ): ApiResponse<ReportDetailDto>

    @DELETE("v1/laporan/{id}")
    suspend fun deleteReport(
        @Path("id") id: String
    ): Response<ApiResponse<Any?>>

    @Multipart
    @POST("v1/laporan/{id}")
    suspend fun submitReport(
        @Path("id") id: String,
        @Part("_method") method: RequestBody, // "PATCH"
        @Part("status") status: RequestBody   // "menunggu"
    ): ApiResponse<Any?>

    @Multipart
    @POST("v1/laporan")
    suspend fun createReport(
        @Part("data") reportData: RequestBody,
        @Part attachments: List<MultipartBody.Part>
    ): Response<ApiResponse<Any?>>

    @Multipart
    @POST("v1/laporan/{id}")
    suspend fun updateReport(
        @Path("id") id: String,
        @Part parts: List<MultipartBody.Part>
    ): Response<ApiResponse<Unit>>
}