package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class KthDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("nama_kth") val name: String,
    @SerializedName("desa") val desa: String,
    @SerializedName("kecamatan") val kecamatan: String?,
    @SerializedName("kabupaten") val kabupaten: String,
    @SerializedName("koordinator") val coordinator: String?,
    @SerializedName("nomor_wa") val whatsappNumber: String?,
    @SerializedName("asal_kph") val kphName: String
)