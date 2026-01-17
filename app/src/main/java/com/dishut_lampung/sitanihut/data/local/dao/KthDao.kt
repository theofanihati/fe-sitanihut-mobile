package com.dishut_lampung.sitanihut.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dishut_lampung.sitanihut.data.local.entity.KthEntity
import com.dishut_lampung.sitanihut.data.local.entity.PenyuluhEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(kthList: List<KthEntity>)

    @Query("""
        SELECT * FROM kth 
        WHERE name LIKE '%' || :query || '%' 
        OR desa LIKE '%' || :query || '%'
        OR kabupaten LIKE '%' || :query || '%'
        OR kphName LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun getAllKth(query: String): Flow<List<KthEntity>>

    @Query("SELECT * FROM kth WHERE id = :id")
    fun getKthById(id: String): Flow<KthEntity?>

    @Query("DELETE FROM kth WHERE id = :id")
    suspend fun deleteKth(id: String)

    @Query("DELETE FROM kth")
    suspend fun deleteAll()

    @Transaction
    suspend fun updateData(kthList: List<KthEntity>) {
        deleteAll()
        upsertAll(kthList)
    }
}