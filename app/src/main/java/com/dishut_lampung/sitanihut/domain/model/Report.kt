package com.dishut_lampung.sitanihut.domain.model

data class Report(
    val id: String,
    val period: Int,
    val monthPeriod: String,
    val submissionDate: String,
    val totalTransaction: Double,
    val status: ReportStatus
)

data class ReportDetail(
    val id: String,
    val userName: String? = null,
    val userNik: String? = null,
    val userGender: String? = null,
    val userAddress: String? = null,
    val userKphName: String? = null,
    val userKthName: String? = null,
    val month: String,
    val period: Int,
    val modal: String,
    val farmerNotes: String,
    val penyuluhNotes: String,
    val nte: Double,
    val status: ReportStatus,
    val createdAt: String? = null,
    val verifiedAt: String? = null,
    val acceptedAt: String? = null,
    val attachments: List<ReportAttachment>,
    val plantingDetails: List<MasaTanam>,
    val harvestDetails: List<MasaPanen>,
)

data class CreateReportInput(
    val month: String,
    val period: Int,
    val modal: String,
    val plantingDetails: List<MasaTanam>,
    val harvestDetails: List<MasaPanen>,
    val farmerNotes: String,
//    val attachments: List<String>,
    val newAttachments: List<String>,
    val existingAttachmentIds: List<String>,
    val isAjukan: Boolean,
    val nte: Double,
)

data class MasaTanam(
    val commodityId: String,
    val commodityName: String = "",
    val plantType: String,
    val plantDate: String,
    val plantAge: Double,
    val amount: String,
)

data class MasaPanen(
    val harvestDate: String,
    val commodityId: String,
    val commodityName: String = "",
    val unitPrice: String,
    val amount: String,
)

data class ReportAttachment(
    val id: String? = null,
    val filePath: String,
    val isLocal: Boolean,
    val fileName: String? = null
)