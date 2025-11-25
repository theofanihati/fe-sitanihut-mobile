package com.dishut_lampung.sitanihut.domain.model

enum class ReportStatus{
    DRAFT,              // belum diajukan
    PENDING,            // diajukan petani ke penyuluh
    VERIFIED,           // oke di penyuluh, ajukan ke PJ
    APPROVED,           // oke di pj
    REJECTED,           // ditolak
}