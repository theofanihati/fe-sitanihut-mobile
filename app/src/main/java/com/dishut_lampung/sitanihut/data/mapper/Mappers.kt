package com.dishut_lampung.sitanihut.data.mapper

import com.dishut_lampung.sitanihut.data.local.entity.CommodityEntity
import com.dishut_lampung.sitanihut.data.local.entity.KphEntity
import com.dishut_lampung.sitanihut.data.local.entity.KthEntity
import com.dishut_lampung.sitanihut.data.local.entity.PenyuluhEntity
import com.dishut_lampung.sitanihut.data.local.entity.PetaniEntity
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.RoleEntity
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity
import com.dishut_lampung.sitanihut.data.remote.dto.CommodityDto
import com.dishut_lampung.sitanihut.data.remote.dto.CreateKthRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.CreatePetaniRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.CreateUserRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.HarvestRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.HarvestResponseDto
import com.dishut_lampung.sitanihut.data.remote.dto.KphDto
import com.dishut_lampung.sitanihut.data.remote.dto.KthDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.KthListItemDto
import com.dishut_lampung.sitanihut.data.remote.dto.PetaniDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.PetaniListItemDto
import com.dishut_lampung.sitanihut.data.remote.dto.PlantingRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.PlantingResponseDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportRequestDto
import com.dishut_lampung.sitanihut.data.remote.dto.RoleDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserListItemDto
import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.model.MasaPanen
import com.dishut_lampung.sitanihut.domain.model.MasaTanam
import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportAttachment
import com.dishut_lampung.sitanihut.domain.model.ReportDetail
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.Role
import com.dishut_lampung.sitanihut.domain.model.User
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
        role = this.role?: "",
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

fun UserEntity.toDomain(): UserDetail {
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

fun UserListItemDto.toEntity(): UserEntity{
    return UserEntity(
        id = id,
        name = name,
        gender = gender,
        role = role,
        kphId = kphId,
        kphName = kphName,
        kthId = kthId,
        kthName = kthName
    )
}

fun UserDetailDto.toDomain(roleNameResolved: String): UserDetail{
    return UserDetail(
        id = id,
        email = email,
        role = roleNameResolved,
        roleId = roleId,
        name = name,
        identityNumber = identityNumber,
        gender = gender,
        address = address,
        whatsAppNumber = whatsAppNumber,
        lastEducation = lastEducation,
        sideJob = sideJob,
        landArea = landArea,
        kphId = kphId,
        kphName = kphName,
        kthId = kthId,
        kthName = kthName,
    )
}

fun CreateUserInput.toDto(): CreateUserRequestDto {
    return CreateUserRequestDto(
        email = this.email,
        password = this.password,
        roleId = this.roleId,
        kphId = this.kphId,
        kthId = this.kthId,
        name = this.name,
        identityNumber = this.identityNumber,
        gender = this.gender,
        address = this.address,
        whatsAppNumber = this.whatsAppNumber,
        lastEducation = this.lastEducation,
        sideJob = this.sideJob,
        landArea = this.landArea,
//        position = this.position
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

fun ReportEntity.toReportDetail(): ReportDetail {
    val gson = Gson()
    val plantingList: List<MasaTanam> = try {
        if (!this.plantingDetailsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<MasaTanam>>() {}.type
            gson.fromJson(this.plantingDetailsJson, type)
        } else emptyList()
    } catch (e: Exception) { emptyList() }

    val harvestList: List<MasaPanen> = try {
        if (!this.harvestDetailsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<MasaPanen>>() {}.type
            gson.fromJson(this.harvestDetailsJson, type)
        } else emptyList()
    } catch (e: Exception) { emptyList() }

    val attachmentList: List<ReportAttachment> = try {
        if (!this.attachmentsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<ReportAttachment>>() {}.type
            gson.fromJson(this.attachmentsJson, type)
        } else emptyList()
    } catch (e: Exception) { emptyList() }

    val modalString = this.modal?.let {
        if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
    } ?: ""

    val statusEnum = when (this.status?.lowercase(Locale.ROOT)) {
        "disetujui" -> ReportStatus.APPROVED
        "ditolak" -> ReportStatus.REJECTED
        "diverifikasi" -> ReportStatus.VERIFIED
        "menunggu" -> ReportStatus.PENDING
        "belum diajukan" -> ReportStatus.DRAFT
        else -> ReportStatus.DRAFT
    }

    return ReportDetail(
        id = this.id,
        userName = this.userName ?: "-",
        userNik = this.userNik ?: "-",
        userGender = this.userGender ?: "-",
        userAddress = this.userAddress ?: "-",
        userKphName = this.userKphName ?: "-",
        userKthName = this.userKthName ?: "-",
        month = this.month,
        period = this.period,
        modal = modalString,
        farmerNotes = this.farmerNotes ?: "",
        penyuluhNotes = this.penyuluhNotes ?: "",
        createdAt = this.createdAt ?: "-",
        verifiedAt = this.verifiedAt ?: "-",
        acceptedAt = this.acceptedAt ?: "-",
        nte = this.nte,
        status = statusEnum,
        attachments = attachmentList,
        plantingDetails = plantingList,
        harvestDetails = harvestList
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

fun ReportDetailDto.toEntity(): ReportEntity {
    val gson = Gson()

    val plantingJson = if (!plantingDetails.isEmpty()) {
        gson.toJson(plantingDetails.map { it.toDomain() })
    } else null

    val harvestJson = if (!harvestDetails.isEmpty()) {
        gson.toJson(harvestDetails.map { it.toDomain() })
    } else null

    val attachmentList = this.attachments?.map { dto ->
        ReportAttachment(
            id = dto.attachmentId,
            filePath = dto.url,
            isLocal = false
        )
    } ?: emptyList()
    val attachmentsJsonString = gson.toJson(attachmentList)

    return ReportEntity(
        id = this.id,
        userId = this.userId,
        userName = this.userName,
        userNik = this.userNik,
        userGender = this.userGender,
        userAddress = this.userAddress,
        userKphName = this.userKphName,
        userKthName = this.userKthName,
        period = this.period,
        month = this.month,
        date = this.date,
        nte = this.nte,
        status = this.status,
        modal = this.modal,
        farmerNotes = this.farmerNotes,
        penyuluhNotes = this.penyuluhNotes,
        createdAt = this.createdAt,
        verifiedAt = this.verifiedAt,
        acceptedAt = this.acceptedAt,
        plantingDetailsJson = plantingJson,
        harvestDetailsJson = harvestJson,
        attachmentsJson = attachmentsJsonString,
        syncStatus = SyncStatus.SYNCED,
        jsonPayload = null
    )
}

fun PlantingResponseDto.toDomain(): MasaTanam {
    val hasDate = !this.date.isNullOrEmpty() && this.date != "-"
    val inferredType = if (hasDate) "Semusim" else "Tahunan"

    return MasaTanam(
        commodityId = this.commodityId,
        commodityName = this.commodityName ?: "",
        plantType = inferredType,
        plantDate = if (hasDate) this.date else "",
        plantAge = this.plantAge,
        amount = this.amount.toString()
    )
}

fun HarvestResponseDto.toDomain(): MasaPanen {
    return MasaPanen(
        harvestDate = this.date,
        commodityId = this.commodityId,
        commodityName = this.commodityName ?: "",
        unitPrice = this.unitPrice.toString(),
        amount = this.amount.toString()
    )
}

fun RoleDto.toEntity(): RoleEntity {
    return RoleEntity(
        id = this.id,
        name = this.name
    )
}

fun RoleEntity.toDomain(): Role{
    return Role(
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
        nte = this.nte,
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

fun PenyuluhEntity.toDomain(): Penyuluh {
    return Penyuluh(
        id = id,
        name = name,
        identityNumber = identityNumber,
        position = position,
        gender = gender,
        kphId = kphId,
        kphName = kphName,
        whatsAppNumber = whatsAppNumber
    )
}

fun KthEntity.toDomain(): Kth {
    return Kth(
        id = id,
        name = name,
        desa = desa,
        kecamatan = kecamatan,
        kabupaten = kabupaten,
        coordinator = coordinator,
        whatsappNumber = whatsappNumber,
        kphId = kphId,
        kphName = kphName
    )
}

fun KthListItemDto.toEntity(): KthEntity {
    return KthEntity(
        id = id,
        name = name,
        desa = desa,
        kabupaten = kabupaten,
        kphId = kphId,
        kphName = kphName
    )
}

fun KthDetailDto.toEntity(): KthEntity {
    return KthEntity(
        id = id,
        name = name,
        desa = desa,
        kecamatan = kecamatan,
        kabupaten = kabupaten,
        coordinator = coordinator,
        whatsappNumber = whatsappNumber,
        kphId = kphId,
        kphName = kphName
    )
}
fun KthDetailDto.toDomain(): Kth {
    return Kth(
        id = id,
        name = name,
        desa = desa,
        kecamatan = kecamatan,
        kabupaten = kabupaten,
        coordinator = coordinator,
        whatsappNumber = whatsappNumber,
        kphName = kphName,
        kphId = kphId
    )
}

fun CreateKthInput.toDto(): CreateKthRequestDto {
    return CreateKthRequestDto(
        name = this.name,
        desa = this.desa,
        kecamatan = this.kecamatan,
        kabupaten = this.kabupaten,
        coordinator = this.coordinator,
        whatsappNumber = this.whatsappNumber,
        kphId = this.kphId
    )
}

fun KphEntity.toDomain(): Kph = Kph(
    id = id,
    name = name
)
fun KphDto.toEntity(): KphEntity {
    return KphEntity(
        id = id,
        name = name
    )
}

fun PetaniEntity.toDomain(): Petani {
    return Petani(
        id = id,
        name = name,
        identityNumber = identityNumber,
        gender = gender,
        address = address,
        whatsAppNumber = whatsAppNumber,
        lastEducation = lastEducation,
        sideJob = sideJob,
        landArea = landArea,
        kphId = kphId,
        kphName = kphName,
        kthId = kthId,
        kthName = kthName
    )
}

fun PetaniListItemDto.toEntity(): PetaniEntity{
    return PetaniEntity(
        id = id,
        name = name,
        identityNumber = identityNumber,
        kthName = kthName,
        kthId = kthId,
        kphName = kphName,
        kphId = kphId,
    )
}

fun PetaniDetailDto.toEntity(): PetaniEntity{
    return PetaniEntity(
        id = id,
        name = name,
        identityNumber = identityNumber,
        gender = gender,
        address = address,
        whatsAppNumber = whatsAppNumber,
        lastEducation = lastEducation,
        sideJob = sideJob,
        landArea = landArea,
        kphId = kphId,
        kphName = kphName,
        kthId = kthId,
        kthName = kthName
    )
}

fun PetaniDetailDto.toDomain(): Petani{
    return Petani(
        id = id,
        name = name,
        identityNumber = identityNumber,
        gender = gender,
        address = address,
        whatsAppNumber = whatsAppNumber,
        lastEducation = lastEducation,
        sideJob = sideJob,
        landArea = landArea,
        kphId = kphId,
        kphName = kphName,
        kthId = kthId,
        kthName = kthName,
    )
}

fun CreatePetaniInput.toDto(): CreatePetaniRequestDto{
    return CreatePetaniRequestDto(
        name = this.name,
        identityNumber = this.identityNumber,
        gender = this.gender,
        address = this.address,
        whatsAppNumber = this.whatsAppNumber,
        lastEducation = this.lastEducation,
        sideJob = this.sideJob,
        landArea = this.landArea,
        kthId = this.kthId,
    )
}
