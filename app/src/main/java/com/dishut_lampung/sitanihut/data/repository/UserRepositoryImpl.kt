package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.RoleDao
import com.dishut_lampung.sitanihut.data.local.dao.UserDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toDto
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.UserApiService
import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
    private val dao: UserDao,
    private val roleDao: RoleDao,
    private val userPreferences: UserPreferences
): UserRepository {

    override fun getUserList(query: String): Flow<Resource<List<UserDetail>>> {
        return TODO()
    }

    override suspend fun syncUserData(): Resource<Unit> {
        return TODO()
    }

    override suspend fun deleteUser(id: String): Resource<Unit> {
        return TODO()
    }

    override fun getUserDetail(id: String): Flow<Resource<UserDetail>> = flow {
        TODO()
    }

    override suspend fun createUser(input: CreateUserInput): Resource<Unit> {
        return TODO()
    }

    override suspend fun updateUser(id: String, changes: Map<String, Any?>): Resource<Unit> {
        return TODO()
    }

    private suspend fun resolveRoleName(roleId: String): String {
        TODO()
    }

    private suspend fun fetchAndCacheRoles() {
        TODO()
    }
}