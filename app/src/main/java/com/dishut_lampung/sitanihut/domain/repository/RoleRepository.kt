package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.Role
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

interface RoleRepository {
    fun getRoles(): Flow<Resource<List<Role>>>
    suspend fun syncRoleData(): Resource<Unit>
}