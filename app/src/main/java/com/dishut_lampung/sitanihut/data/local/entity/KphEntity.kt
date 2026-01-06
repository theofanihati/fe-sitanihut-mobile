package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kph")
data class KphEntity(
    @PrimaryKey val id: String,
    val name: String
)