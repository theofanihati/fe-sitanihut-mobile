package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.RoleDao
import com.dishut_lampung.sitanihut.data.local.entity.RoleEntity
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.RoleApiService
import com.dishut_lampung.sitanihut.domain.model.Role
import com.dishut_lampung.sitanihut.domain.repository.RoleRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoleRepositoryImpl @Inject constructor(
    private val api: RoleApiService,
    private val dao: RoleDao
) : RoleRepository {

    override fun getRoles(): Flow<Resource<List<Role>>> = flow {
        TODO()
    }

    override suspend fun syncRoleData(): Resource<Unit> {
        return TODO()
    }
}