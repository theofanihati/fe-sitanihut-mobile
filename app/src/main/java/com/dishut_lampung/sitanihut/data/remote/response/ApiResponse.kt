package com.dishut_lampung.sitanihut.data.remote.response

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T
)

data class PaginatedData<T>(
    @SerializedName("data")
    val data: List<T>,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("count")
    val count: Int
)
