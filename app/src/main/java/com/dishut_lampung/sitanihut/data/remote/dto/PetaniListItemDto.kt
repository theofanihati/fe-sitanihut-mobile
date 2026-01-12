package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PetaniListItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("nama_petani") val name: String,
    @SerializedName("nik") val identityNumber: String,
    @SerializedName("id_kth") val kthId: String,
    @SerializedName("asal_kth") val kthName: String,
    @SerializedName("id_kph") val kphId: String,
    @SerializedName("nama_kph") val kphName: String,
)