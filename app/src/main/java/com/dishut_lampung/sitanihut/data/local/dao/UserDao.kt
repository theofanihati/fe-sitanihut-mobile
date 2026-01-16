package com.dishut_lampung.sitanihut.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao{
    @Upsert
    suspend fun upsertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(petaniList: List<UserEntity>)

    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("""
        SELECT * FROM user 
        WHERE name LIKE '%' || :query || '%' 
        OR identityNumber LIKE '%' || :query || '%'
        OR kphName LIKE '%' || :query || '%'
        OR kthName LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun getAllUser(query: String): Flow<List<UserEntity>>

    @Query("DELETE FROM user WHERE id = :id")
    suspend fun deleteUser(id: String)
}