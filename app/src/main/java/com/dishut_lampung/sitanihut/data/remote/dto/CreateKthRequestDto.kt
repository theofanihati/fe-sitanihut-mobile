package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateKthRequestDto(
    @SerializedName("nama_kth") val name: String,
    @SerializedName("desa") val desa: String,
    @SerializedName("kecamatan") val kecamatan: String,
    @SerializedName("kabupaten") val kabupaten: String,
    @SerializedName("koordinator") val coordinator: String,
    @SerializedName("nomor_wa") val whatsappNumber: String,
    @SerializedName("id_kph") val kphId: String
)