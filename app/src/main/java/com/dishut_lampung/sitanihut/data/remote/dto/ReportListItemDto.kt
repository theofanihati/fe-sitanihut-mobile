package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReportListItemDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("tanggal")
    val date: String,
    @SerializedName("periode")
    val period: Int,
    @SerializedName("bulan")
    val month: String,
    @SerializedName("nte")
    val nte: Double,
    @SerializedName("status")
    val status: String,
    @SerializedName("id_user")
    val userId: String,
    @SerializedName("nama_user")
    val userName: String
)