package com.dishut_lampung.sitanihut.domain.model

import java.io.File

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
    val month: String,
    val period: Int,
    val modal: String,
    val farmerNotes: String,
    val nte: Double,
    val status: ReportStatus,
    val attachments: List<String>,
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
    val attachments: List<String>,
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