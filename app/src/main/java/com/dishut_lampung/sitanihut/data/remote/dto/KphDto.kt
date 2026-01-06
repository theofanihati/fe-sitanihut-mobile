package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class KphDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("nama_kph")
    val name: String
)