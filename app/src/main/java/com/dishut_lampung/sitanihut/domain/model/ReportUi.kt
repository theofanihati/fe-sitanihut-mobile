package com.dishut_lampung.sitanihut.domain.model

import java.text.NumberFormat
import java.util.Locale

data class ReportUiModel(
    val id: String,
    val periodTitle: String,
    val dateDisplay: String,
    val nteDisplay: String,
    val statusDisplay: String,
    val isEditable: Boolean,
    val isDeletable: Boolean,
    val domainStatus: ReportStatus
)

fun Report.toUiModel(): ReportUiModel {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    val nteFormatted = try {
        formatter.format(this.totalTransaction.toDouble())
    } catch (e: Exception) {
        "Rp0"
    }

    val statusText = when (this.status) {
        ReportStatus.APPROVED -> "Disetujui"
        ReportStatus.REJECTED -> "Ditolak"
        ReportStatus.PENDING -> "Menunggu"
        ReportStatus.DRAFT -> "Draft"
    }

    val isEditable = this.status == ReportStatus.DRAFT || this.status == ReportStatus.REJECTED
    val isDeletable = this.status == ReportStatus.DRAFT || this.status == ReportStatus.REJECTED
    val periodTitle = "Laporan Periode ${this.monthPeriod} ${this.period}"

    return ReportUiModel(
        id = this.id,
        periodTitle = periodTitle,
        dateDisplay = this.submissionDate,
        nteDisplay = nteFormatted,
        statusDisplay = statusText,
        isEditable = isEditable,
        domainStatus = this.status,
        isDeletable = isDeletable
    )
}
