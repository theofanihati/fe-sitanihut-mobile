package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDetailDto(
    @SerializedName("id_user")
    val id: String,
    @SerializedName("nama_user")
    val name: String,
    @SerializedName("profile_picture_url")
    val profilePictureUrl: String?
)