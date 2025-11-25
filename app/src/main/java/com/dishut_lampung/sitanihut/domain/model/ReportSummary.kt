package com.dishut_lampung.sitanihut.domain.model

data class ReportSummary(
    val pendingCount: Int,
    val verifiedcount: Int,
    val approvedCount: Int,
    val rejectedCount: Int
)