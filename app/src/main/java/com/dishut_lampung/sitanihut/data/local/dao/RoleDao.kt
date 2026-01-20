package com.dishut_lampung.sitanihut.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dishut_lampung.sitanihut.data.local.entity.KphEntity
import com.dishut_lampung.sitanihut.data.local.entity.RoleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RoleEntity>)

    @Query("SELECT name FROM role WHERE id = :roleId")
    suspend fun getRoleName(roleId: String): String?

    @Query("SELECT * FROM role")
    fun getAllRoles(): Flow<List<RoleEntity>>

    @Query("DELETE FROM role")
    suspend fun deleteAll()

    @Transaction
    suspend fun updateData(roles: List<RoleEntity>) {
        deleteAll()
        insertRoles(roles)
    }
}