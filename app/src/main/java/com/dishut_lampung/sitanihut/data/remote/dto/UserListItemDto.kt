package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserListItemDto (
    @SerializedName("id") val id: String,
    @SerializedName("nama_user") val name: String,
    @SerializedName("jenis_kelamin") val gender: String,
    @SerializedName("role") val role: String,
    @SerializedName("id_kph") val kphId: String,
    @SerializedName("nama_kph") val kphName: String,
    @SerializedName("id_kth") val kthId: String,
    @SerializedName("nama_kth") val kthName: String,
)