package com.dishut_lampung.sitanihut.data.remote.api

import com.dishut_lampung.sitanihut.data.remote.dto.CommodityDto
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import retrofit2.http.GET
import retrofit2.http.Query

interface CommodityApiService {
    @GET("v1/komoditas")
    suspend fun getCommodities(
        @Query("search") search: String? = null,
    ): PaginatedData<CommodityDto>
}