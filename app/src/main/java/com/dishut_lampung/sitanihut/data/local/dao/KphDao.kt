package com.dishut_lampung.sitanihut.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dishut_lampung.sitanihut.data.local.entity.KphEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KphDao {
    @Query("SELECT * FROM kph")
    fun getAllKph(): Flow<List<KphEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(kphList: List<KphEntity>)

    @Query("DELETE FROM kph")
    suspend fun deleteAll()

    @Transaction
    suspend fun updateData(kphList: List<KphEntity>) {
        deleteAll()
        insertAll(kphList)
    }
}