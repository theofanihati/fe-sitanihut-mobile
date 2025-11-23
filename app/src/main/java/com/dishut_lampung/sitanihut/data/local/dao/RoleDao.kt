package com.dishut_lampung.sitanihut.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dishut_lampung.sitanihut.data.local.entity.RoleEntity

@Dao
interface RoleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoles(roles: List<RoleEntity>)

    @Query("SELECT name FROM role WHERE id = :roleId")
    suspend fun getRoleName(roleId: String): String?
}