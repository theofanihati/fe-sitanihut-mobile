package com.dishut_lampung.sitanihut.data.mapper

import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDto
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.ReportUiModel
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

fun UserDto.toEntity(role: String): UserEntity {
    return UserEntity(
        id = this.id,
        name = this.name,
        role = role,
        profilePictureUrl = this.profilePictureUrl
    )
}

fun ReportListItemDto.toEntity(): ReportEntity {
    return ReportEntity(
        id = this.id,
        userId = this.userId,
        period = this.period,
        month = this.month,
        date = this.date,
        nte = this.nte,
        status = this.status,
        syncStatus = "SYNCED"
    )
}

fun UserEntity.toDomain(): UserProfile {
    return UserProfile(
        name = this.name,
        role = this.role,
        profilePictureUrl = this.profilePictureUrl
    )
}

fun ReportEntity.toDomain(): Report {
    val status = when (this.status.lowercase(Locale.ROOT)) {
        "disetujui" -> ReportStatus.APPROVED
        "ditolak" -> ReportStatus.REJECTED
        "menunggu" -> ReportStatus.PENDING
        "belum diajukan" -> ReportStatus.DRAFT
        else -> ReportStatus.DRAFT
    }

    val formattedDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        parser.parse(this.date)?.let { formatter.format(it) } ?: this.date
    } catch (e: Exception) {
        this.date
    }

    return Report(
        id = this.id,
        period = period,
        monthPeriod = month,
        submissionDate = formattedDate,
        totalTransaction = this.nte,
        status = status
    )
}

fun ReportListItemDto.toDomain(): Report {
    val statusEnum = when (this.status?.lowercase(Locale.ROOT)) {
        "disetujui" -> ReportStatus.APPROVED
        "ditolak" -> ReportStatus.REJECTED
        "menunggu" -> ReportStatus.PENDING
        "belum diajukan" -> ReportStatus.DRAFT
        else -> ReportStatus.DRAFT
    }

    val formattedDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        this.date?.let { parser.parse(it)?.let { dateObj -> formatter.format(dateObj) } }
            ?: (this.date ?: "")
    } catch (e: Exception) {
        this.date ?: ""
    }

    return Report(
        id = this.id ?: "",
        period = this.period ?: 0,
        monthPeriod = this.month ?: "",
        submissionDate = formattedDate,
        totalTransaction = this.nte ?: 0.0,
        status = statusEnum
    )
}

//fun Report.toUiModel(): ReportUiModel {
//    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
//    val nteFormatted = formatRp.format(this.totalTransaction)
//
//    val statusString = when (this.status) {
//        ReportStatus.APPROVED -> "Disetujui"
//        ReportStatus.REJECTED -> "Ditolak"
//        ReportStatus.PENDING -> "Menunggu"
//        ReportStatus.DRAFT -> "Draft"
//    }
//
//    return ReportUiModel(
//        id = this.id,
//        periodTitle = "Laporan Periode ${this.monthPeriod} ${this.period}",
//        dateDisplay = this.submissionDate,
//        nteDisplay = nteFormatted,
//        statusDisplay = statusString,
//        domainStatus = this.status
//    )
//}