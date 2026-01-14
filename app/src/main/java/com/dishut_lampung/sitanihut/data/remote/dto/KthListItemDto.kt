package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class KthListItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("nama_kth") val name: String,
    @SerializedName("desa") val desa: String,
    @SerializedName("kabupaten") val kabupaten: String,
    @SerializedName("id_kph") val kphId: String,
    @SerializedName("asal_kph") val kphName: String
)