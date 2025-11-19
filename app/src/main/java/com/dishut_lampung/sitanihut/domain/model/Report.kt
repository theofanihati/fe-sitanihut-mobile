package com.dishut_lampung.sitanihut.domain.model

data class Report(
    val id: String,
    val period: Int,
    val monthPeriod: String,
    val submissionDate: String,
    val totalTransaction: Float, //TODO ini float atau string, namanya mau nte bukan
    val status: ReportStatus
)