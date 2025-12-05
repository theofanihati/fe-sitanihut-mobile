package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CommodityDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("kode")
    val code: String,
    @SerializedName("nama_komoditas")
    val name: String,
    @SerializedName("jenis_komoditas")
    val category: String,
    @SerializedName("created_at")
    val createdAt: String
)