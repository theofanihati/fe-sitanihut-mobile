package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "laporan")
data class ReportEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val userId: String,
    val period: Int,
    val month: String,
    val date: String,
    val nte: Double,
    val status: String,
    val capital: Double? = null,
    val farmerNotes: String? = null,
    val jsonPayload: String? = null,
    val plantingDetailsJson: String? = null,
    val harvestDetailsJson: String? = null,
    val attachmentPaths: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

enum class SyncStatus {
    SYNCED,         // aman di server
    PENDING_CREATE, // baru dibuat offline, belum naik
    PENDING_UPDATE, // diedit offline
    PENDING_DELETE  // dihapus offline (soft delete ya bre)
}