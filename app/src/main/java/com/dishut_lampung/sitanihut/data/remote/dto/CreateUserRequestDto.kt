package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateUserRequestDto (
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("id_role") val roleId: String,
    @SerializedName("id_kph") val kphId: String,
    @SerializedName("id_kth") val kthId: String,
    @SerializedName("nama_user") val name: String,
    @SerializedName("nomor_induk") val identityNumber: String,
    @SerializedName("jenis_kelamin") val gender: String,
    @SerializedName("alamat") val address: String,
    @SerializedName("nomor_wa") val whatsAppNumber: String,
    @SerializedName("pendidikan_terakhir") val lastEducation: String,
    @SerializedName("pekerjaan_sampingan") val sideJob: String,
    @SerializedName("luas_lahan") val landArea: String,
//    @SerializedName("jabatan") val position: String,
)