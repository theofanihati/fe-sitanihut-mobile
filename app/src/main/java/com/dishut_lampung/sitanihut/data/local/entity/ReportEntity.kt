package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "laporan")
data class ReportEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val period: Int,
    val month: String,
    val date: String,
    val nte: Double,
    val status: String,
    val syncStatus: String
)