package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val role: String,
    val profilePictureUrl: String? = null,

    val email: String? = null,
    val roleId: String? = null,
    val kphId: String? = null,
    val kphName: String? = null,
    val kthId: String? = null,
    val kthName: String? = null,
    val identityNumber: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val whatsAppNumber: String? = null,
    val lastEducation: String? = null,
    val sideJob: String? = null,
    val landArea: Double? = null,
    val position: String? = null
)