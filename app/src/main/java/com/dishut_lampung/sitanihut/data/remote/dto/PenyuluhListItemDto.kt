package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PenyuluhListItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("nama_penyuluh") val name: String,
    @SerializedName("nip") val identityNumber: String,
    @SerializedName("jabatan") val position: String,
    @SerializedName("jenis_kelamin") val gender: String,
    @SerializedName("id_kph") val kphId: String,
    @SerializedName("asal_kph") val kphName: String,
)