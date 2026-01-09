package com.dishut_lampung.sitanihut.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dishut_lampung.sitanihut.data.local.entity.PetaniEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetaniDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(petaniList: List<PetaniEntity>)

    @Query("""
        SELECT * FROM petani 
        WHERE name LIKE '%' || :query || '%' 
        OR identityNumber LIKE '%' || :query || '%'
        OR kphName LIKE '%' || :query || '%'
        OR kthName LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun getAllPetani(query: String): Flow<List<PetaniEntity>>

    @Query("SELECT * FROM petani WHERE id = :id")
    fun getPetaniById(id: String): Flow<PetaniEntity?>

    @Query("DELETE FROM petani WHERE id = :id")
    suspend fun deletePetani(id: String)
}