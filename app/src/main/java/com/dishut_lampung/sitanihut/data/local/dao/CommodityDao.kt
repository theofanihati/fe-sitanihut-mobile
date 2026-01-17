package com.dishut_lampung.sitanihut.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dishut_lampung.sitanihut.data.local.entity.CommodityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommodityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommodities(commodities: List<CommodityEntity>)

    @Query("""
        SELECT * FROM commodities 
        WHERE name LIKE '%' || :query || '%' 
        OR code LIKE '%' || :query || '%'
        OR category LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun getCommodities(query: String): Flow<List<CommodityEntity>>

    @Query("DELETE FROM commodities")
    suspend fun deleteAll()

    @Transaction
    suspend fun updateData(commodities: List<CommodityEntity>) {
        deleteAll()
        insertCommodities(commodities)
    }
}