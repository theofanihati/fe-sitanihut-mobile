package com.dishut_lampung.sitanihut.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao{
    @Upsert
    suspend fun upsertUser(user: UserEntity)

    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>
}

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertOrUpdate(user: UserEntity)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(users: List<UserEntity>)
//
//    @Query("SELECT * FROM users")
//    fun getAllUsers(): Flow<List<UserEntity>>


//    @Query("DELETE FROM users")
//    suspend fun clearAll()
//
//    @Query("DELETE FROM users WHERE id = :userId")
//    suspend fun deleteUserById(userId: String)
//
//    @Query("SELECT * FROM users WHERE is_synced = 0")
//    suspend fun getUnsyncedUsers(): List<UserEntity>