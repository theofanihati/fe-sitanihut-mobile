package com.dishut_lampung.sitanihut.data.mapper

import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDetailDto
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Locale

fun UserDetailDto.toEntity(role: String): UserEntity {
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
        "belum diajukan" -> ReportStatus.PENDING
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
