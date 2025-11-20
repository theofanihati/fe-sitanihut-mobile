package com.dishut_lampung.sitanihut.domain.model

data class Report(
    val id: String,
    val period: Int,
    val monthPeriod: String,
    val submissionDate: String,
    val totalTransaction: Double,
    val status: ReportStatus
)