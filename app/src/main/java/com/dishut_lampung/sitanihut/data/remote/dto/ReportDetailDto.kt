package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReportDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("id_user") val userId: String,
    @SerializedName("periode") val period: Int,
    @SerializedName("bulan") val month: String,
    @SerializedName("tanggal") val date: String,
    @SerializedName("modal") val modal: Double,
    @SerializedName("nte") val nte: Double,
    @SerializedName("status") val status: String,
    @SerializedName("tanggal_pengajuan") val createdAt: String,
    @SerializedName("tanggal_tindakan") val verifiedAt: String,
    @SerializedName("tanggal_keputusan") val acceptedAt: String,
    @SerializedName("lampiran") val attachments: List<AttachmentDto>?,
    @SerializedName("catatan_petani") val farmerNotes: String?,
    @SerializedName("catatan_penyuluh") val penyuluhNotes: String?,
    @SerializedName("masa_tanam") val plantingDetails: List<PlantingResponseDto>,
    @SerializedName("masa_panen") val harvestDetails: List<HarvestResponseDto>,
)

data class PlantingResponseDto(
    @SerializedName("tanggal") val date: String,
    @SerializedName("id_komoditas") val commodityId: String,
    @SerializedName("nama_komoditas") val commodityName: String?,
    @SerializedName("kode_komoditas") val commodityCode: String?,
    @SerializedName("jenis_komoditas") val plantType: String?,
    @SerializedName("jumlah") val amount: Double,
    @SerializedName("usia_tanam") val plantAge: Double,
)

data class HarvestResponseDto(
    @SerializedName("tanggal") val date: String,
    @SerializedName("id_komoditas") val commodityId: String,
    @SerializedName("nama_komoditas") val commodityName: String?,
    @SerializedName("kode_komoditas") val commodityCode: String?,
    @SerializedName("jenis_komoditas") val plantType: String?,
    @SerializedName("jumlah") val amount: Double,
    @SerializedName("harga_satuan") val unitPrice: Double,
    @SerializedName("harga_total") val totalPrice: Double,
)

data class AttachmentDto(
    @SerializedName("id") val attachmentId: String,
    @SerializedName("nama_file") val attachmentName: String,
    @SerializedName("url") val url: String,
)