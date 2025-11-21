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
