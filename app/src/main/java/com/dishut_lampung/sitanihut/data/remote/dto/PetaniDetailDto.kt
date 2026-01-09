package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PetaniDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("nama_petani") val name: String,
    @SerializedName("nik") val identityNumber: String,
    @SerializedName("jenis_kelamin") val gender: String?,
    @SerializedName("alamat") val address: String?,
    @SerializedName("nomor_wa") val whatsAppNumber: String?,
    @SerializedName("pendidikan_terakhir") val lastEducation: String?,
    @SerializedName("pekerjaan_sampingan") val sideJob: String?,
    @SerializedName("luas_lahan") val landArea: Double?,
    @SerializedName("id_kth") val kthId: String?,
    @SerializedName("asal_kth") val kthName: String,
    @SerializedName("id_kph") val kphId: String?,
    @SerializedName("nama_kph") val kphName: String,
)