package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kth")
data class KthEntity(
    @PrimaryKey val id: String,
    val name: String,
    val desa: String,
    val kecamatan: String? = null,
    val kabupaten: String,
    val coordinator: String? = null,
    val whatsappNumber: String? = null,
    val kphName: String,
    val kphId: String? = null,
)