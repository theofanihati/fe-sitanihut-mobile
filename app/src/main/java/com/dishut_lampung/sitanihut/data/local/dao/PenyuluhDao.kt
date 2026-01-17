package com.dishut_lampung.sitanihut.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dishut_lampung.sitanihut.data.local.entity.CommodityEntity
import com.dishut_lampung.sitanihut.data.local.entity.PenyuluhEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PenyuluhDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(penyuluh: List<PenyuluhEntity>)

    @Query("""
        SELECT * FROM penyuluh 
        WHERE name LIKE '%' || :query || '%' 
        OR identityNumber LIKE '%' || :query || '%'
        OR position LIKE '%' || :query || '%'
        OR kphName LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun getAllPenyuluh(query: String): Flow<List<PenyuluhEntity>>

    @Query("SELECT * FROM penyuluh WHERE id = :id")
    suspend fun getPenyuluhById(id: String): PenyuluhEntity?

    @Query("SELECT * FROM penyuluh WHERE id = :id")
    fun getPenyuluhByIdFlow(id: String): Flow<PenyuluhEntity?>

    @Query("DELETE FROM penyuluh")
    suspend fun deleteAll()

    @Transaction
    suspend fun updateData(penyuluh: List<PenyuluhEntity>) {
        deleteAll()
        upsertAll(penyuluh)
    }
}