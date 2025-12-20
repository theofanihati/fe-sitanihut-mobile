package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "penyuluh")
data class PenyuluhEntity(
    @PrimaryKey val id: String,
    val name: String,
    val identityNumber: String,
    val position: String,
    val gender: String,
    val kphId: String,
    val kphName: String,
    val whatsAppNumber: String? = null
)