package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.RoleDao
import com.dishut_lampung.sitanihut.data.local.dao.UserDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toUserDetail
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.UserApiService
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.repository.ProfileRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
    private val userDao: UserDao,
    private val roleDao: RoleDao,
) : ProfileRepository {
    override fun getUserDetail(userId: String): Flow<Resource<UserDetail>> = flow {
        emit(Resource.Loading())

        val cached = userDao.getUserById(userId).firstOrNull()
        if (cached != null) {
            emit(Resource.Success(cached.toUserDetail()))
        }

        try {
            val response = apiService.getUserDetail(userId)
            val userDto = response.data

            var roleName = roleDao.getRoleName(userDto.roleId)

            if (roleName == null) {
                try {
                    val rolesResponse = apiService.getRoles()
                    val roleEntities = rolesResponse.data.map { it.toEntity() }
                    roleDao.insertRoles(roleEntities)

                    roleName = roleDao.getRoleName(userDto.roleId)
                } catch (e: Exception) {
                    // Silent fail fetch roles
                }
            }
            val finalRoleName = roleName ?: "Pengguna"

            val userEntity = userDto.toEntity(finalRoleName)

            userDao.upsertUser(userEntity)

            val newCached = userDao.getUserById(userId).firstOrNull()
            if (newCached != null) {
                emit(Resource.Success(newCached.toUserDetail()))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Error", cached?.toUserDetail()))
        }
    }
}