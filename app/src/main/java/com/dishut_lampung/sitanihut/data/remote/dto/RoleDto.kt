package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RoleDto(
    val id: String,
    @SerializedName("nama_role") val name: String
)