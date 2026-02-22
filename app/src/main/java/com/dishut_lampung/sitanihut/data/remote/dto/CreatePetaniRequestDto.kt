package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreatePetaniRequestDto (
    @SerializedName("nama_user") val name: String,
    @SerializedName("nik") val identityNumber: String,
    @SerializedName("jenis_kelamin") val gender: String,
    @SerializedName("alamat") val address: String,
    @SerializedName("nomor_wa") val whatsAppNumber: String,
    @SerializedName("pendidikan_terakhir") val lastEducation: String,
    @SerializedName("pekerjaan_sampingan") val sideJob: String,
    @SerializedName("luas_lahan") val landArea: String,
    @SerializedName("id_kth") val kthId: String,
)