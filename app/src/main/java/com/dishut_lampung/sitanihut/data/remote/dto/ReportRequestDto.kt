package com.dishut_lampung.sitanihut.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReportRequestDto(
    @SerializedName("id") val id: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("periode") val period: Int,
    @SerializedName("bulan") val month: String,
    @SerializedName("modal") val modal: Double,
    @SerializedName("nte") val nte: Double,
    @SerializedName("catatan_petani") val farmerNotes: String,
    @SerializedName("status") val status: String,
    @SerializedName("masa_tanam") val plantingDetails: List<PlantingRequestDto>,
    @SerializedName("masa_panen") val harvestDetails: List<HarvestRequestDto>
)

data class PlantingRequestDto(
    @SerializedName("id_komoditas") val commodityId: String,
    @SerializedName("tanggal") val date: String,
    @SerializedName("usia_tanam") val plantAge: Double,
    @SerializedName("jumlah") val amount: Int,
)

data class HarvestRequestDto(
    @SerializedName("id_komoditas") val commodityId: String,
    @SerializedName("tanggal") val date: String,
    @SerializedName("harga_satuan") val unitPrice: Double,
    @SerializedName("jumlah") val amount: Int,
)

data class ConflictResponseDto(
    @SerializedName("error") val error: String,
    @SerializedName("new_uuid") val newUuid: String
)