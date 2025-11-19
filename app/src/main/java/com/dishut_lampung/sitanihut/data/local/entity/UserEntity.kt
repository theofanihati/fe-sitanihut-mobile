package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val role: String,
    val profilePictureUrl: String?,
//    val jenisKelamin: String,
//    val kthName: String,
//    val kphName: String,
)