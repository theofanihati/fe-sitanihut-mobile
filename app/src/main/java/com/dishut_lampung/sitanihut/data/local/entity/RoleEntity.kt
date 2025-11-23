package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "role")
data class RoleEntity(
    @PrimaryKey val id: String,
    val name: String
)