package com.dishut_lampung.sitanihut.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commodities")
data class CommodityEntity(
    @PrimaryKey
    val id: String,
    val code: String,
    val name: String,
    val category: String
)