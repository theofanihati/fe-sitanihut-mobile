package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "laporan")
data class ReportEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val userId: String,
    val userName: String? = null,
    val userNik: String? = null,
    val userGender: String? = null,
    val userAddress: String? = null,
    val userKphName: String? = null,
    val userKthName: String? = null,
    val period: Int,
    val month: String,
    val date: String,
    val nte: Double,
    val status: String,
    val createdAt: String? = null,
    val verifiedAt: String? = null,
    val acceptedAt: String? = null,
    val modal: Double? = null,
    val farmerNotes: String? = null,
    val penyuluhNotes: String? = null,
    val jsonPayload: String? = null,
    val plantingDetailsJson: String? = null,
    val harvestDetailsJson: String? = null,
    val attachmentsJson: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

enum class SyncStatus {
    SYNCED,         // aman di server
    PENDING_CREATE, // baru dibuat offline, belum naik
    PENDING_UPDATE, // diedit offline
    PENDING_DELETE, // dihapus offline (soft delete ya bre)
    PENDING_REVIEW, // utk penyuluh pj verif
}