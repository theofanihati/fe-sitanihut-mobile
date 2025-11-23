package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDetailDto (
    @SerializedName("id_user") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("id_role") val roleId: String,
    @SerializedName("id_kph") val kphId: String? = "",
    @SerializedName("nama_kph") val kphName: String? = "",
    @SerializedName("id_kth") val kthId: String? = "",
    @SerializedName("nama_kth") val kthName: String? = "",
    @SerializedName("nama_user") val name: String,
    @SerializedName("nomor_induk") val identityNumber: String,
    @SerializedName("jenis_kelamin") val gender: String,
    @SerializedName("profile_picture_url") val profilePictureUrl: String? = "",
    @SerializedName("alamat") val address: String? = "",
    @SerializedName("nomor_wa") val whatsAppNumber: String? = "",
    @SerializedName("pendidikan_terakhir") val lastEducation: String? = "",
    @SerializedName("pekerjaan_sampingan") val sideJob: String? = "",
    @SerializedName("luas_lahan") val landArea: Double? = null,
    @SerializedName("jabatan") val position: String? = ""
)