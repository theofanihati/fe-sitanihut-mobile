package com.dishut_lampung.sitanihut.data.mapper

import com.dishut_lampung.sitanihut.data.local.entity.CommodityEntity
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.RoleEntity
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity
import com.dishut_lampung.sitanihut.data.remote.dto.CommodityDto
import com.dishut_lampung.sitanihut.data.remote.dto.HarvestRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.PlantingRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.RoleDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDto
import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.model.MasaPanen
import com.dishut_lampung.sitanihut.domain.model.MasaTanam
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// home profil ringkas
fun UserDto.toEntity(role: String): UserEntity {
    return UserEntity(
        id = this.id,
        name = this.name,
        role = role,
        profilePictureUrl = this.profilePictureUrl,
        email = null, roleId = null, kphId = null, kphName = null,
        kthId = null, kthName = null, identityNumber = null, gender = null,
        address = null, whatsAppNumber = null, lastEducation = null,
        sideJob = null, landArea = null, position = null
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
        syncStatus = SyncStatus.SYNCED
    )
}

// home profil ringkas
fun UserEntity.toUserProfile(): UserProfile {
    return UserProfile(
        name = this.name,
        role = this.role,
        profilePictureUrl = this.profilePictureUrl
    )
}

fun UserDetailDto.toEntity(roleNameResolved: String): UserEntity {
    return UserEntity(
        id = this.id,
        email = this.email,
        roleId = this.roleId,
        role = roleNameResolved,
        kphId = this.kphId,
        kphName = this.kphName,
        kthId = this.kthId,
        kthName = this.kthName,
        name = this.name,
        identityNumber = this.identityNumber,
        gender = this.gender,
        profilePictureUrl = this.profilePictureUrl,
        address = this.address,
        whatsAppNumber = this.whatsAppNumber,
        lastEducation = this.lastEducation,
        sideJob = this.sideJob,
        landArea = this.landArea,
        position = this.position
    )
}

fun UserEntity.toUserDetail(): UserDetail {
    return UserDetail(
        id = this.id,
        email = this.email?: "",
        roleId = this.roleId?: "",
        role = this.role,
        kphId = this.kphId,
        kphName = this.kphName,
        kthId = this.kthId,
        kthName = this.kthName,
        name = this.name,
        identityNumber = this.identityNumber?: "",
        gender = this.gender?: "",
        profilePictureUrl = this.profilePictureUrl,
        address = this.address,
        whatsAppNumber = this.whatsAppNumber,
        lastEducation = this.lastEducation,
        sideJob = this.sideJob,
        landArea = this.landArea,
        position = this.position
    )
}

fun ReportEntity.toDomain(): Report {
    val status = when (this.status.lowercase(Locale.ROOT)) {
        "disetujui" -> ReportStatus.APPROVED
        "ditolak" -> ReportStatus.REJECTED
        "diverifikasi" -> ReportStatus.VERIFIED
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
        "diverifikasi" -> ReportStatus.VERIFIED
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

fun RoleDto.toEntity(): RoleEntity {
    return RoleEntity(
        id = this.id,
        name = this.name
    )
}

fun ReportStatus.toDbValue(): String {
    return when(this) {
        ReportStatus.DRAFT -> "belum diajukan"
        ReportStatus.PENDING -> "menunggu"
        ReportStatus.VERIFIED -> "diverifikasi"
        ReportStatus.APPROVED -> "disetujui"
        ReportStatus.REJECTED -> "ditolak"
    }
}

fun CommodityDto.toEntity(): CommodityEntity {
    return CommodityEntity(
        id = id,
        code = code,
        name = name,
        category = category
    )
}

fun CommodityEntity.toDomain(): Commodity {
    return Commodity(
        id = id,
        code = code,
        name = name,
        category = category
    )
}

fun CreateReportInput.toDto(
    id: String,
    updatedAt: String
): ReportRequestDto {
    return ReportRequestDto(
        id = id,
        updatedAt = updatedAt,
        period = this.period,
        month = this.month,
        modal = this.modal.toDoubleOrNull() ?: 0.0,
        totalNte = this.nte,
        farmerNotes = this.farmerNotes,
        plantingDetails = this.plantingDetails.map { it.toDto() },
        status = if (this.isAjukan) "menunggu" else "belum diajukan",
        harvestDetails = this.harvestDetails.map { it.toDto() }
    )
}

fun MasaTanam.toDto(): PlantingRequestDto {
    return PlantingRequestDto(
        commodityId = this.commodityId,
        date = this.plantDate,
        plantAge = this.plantAge,
        amount = this.amount.toIntOrNull() ?: 0,
    )
}

fun MasaPanen.toDto(): HarvestRequestDto {
    return HarvestRequestDto(
        date = this.harvestDate,
        commodityId = this.commodityId,
        unitPrice = this.unitPrice.toDoubleOrNull() ?: 0.0,
        amount = this.amount.toIntOrNull() ?: 0,
    )
}