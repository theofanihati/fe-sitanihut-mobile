package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.KphDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import retrofit2.http.GET

interface KphApiService {
    @GET("v1/kph")
    suspend fun getKphList(): ApiResponse<List<KphDto>>
}