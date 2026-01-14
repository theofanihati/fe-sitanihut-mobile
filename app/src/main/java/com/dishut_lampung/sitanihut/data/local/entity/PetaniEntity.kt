package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "petani")
data class PetaniEntity(
    @PrimaryKey val id: String,
    val name: String,
    val identityNumber: String,
    val gender: String? = null,
    val address: String? = null,
    val whatsAppNumber: String? = null,
    val lastEducation: String? = null,
    val sideJob: String? = null,
    val landArea: Double? = null,
    val kphName: String,
    val kthName: String,
    val kphId: String,
    val kthId: String,
)