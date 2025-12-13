package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.RoleDao
import com.dishut_lampung.sitanihut.data.local.dao.UserDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toUserDetail
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.UserApiService
import com.dishut_lampung.sitanihut.data.remote.dto.UserDetailDto
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.repository.ProfileRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val apiService: UserApiService,
    private val userDao: UserDao,
    private val roleDao: RoleDao,
    private val userPreferences: UserPreferences
) : ProfileRepository {

    override fun getUserDetail(userId: String): Flow<Resource<UserDetail>> {
        return userDao.getUserById(userId)
            .distinctUntilChanged()
            .map { userEntity ->
            if (userEntity != null) {
                Resource.Success(userEntity.toUserDetail())
            } else {
                Resource.Loading()
            }
        }
    }

    override suspend fun syncUserDetail(): Resource<Unit> {
        return try {
            val currentUserId = userPreferences.userId.first()?: return Resource.Error("No User ID")

            val response = apiService.getUserDetail(currentUserId)
            val userDto = response.data

            val roleName = resolveRoleName(userDto.roleId)
            val userEntity = userDto.toEntity(roleName)

            userDao.upsertUser(userEntity)
            updateUserPreferences(userDto)

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gagal sinkronisasi: ${e.message}")
        }
    }

    private suspend fun resolveRoleName(roleId: String): String {
        roleDao.getRoleName(roleId)?.let { return it }
        try {
            fetchAndCacheRoles()
            roleDao.getRoleName(roleId)?.let { return it }
        } catch (e: Exception) {
        }

        val prefRole = userPreferences.userRole.first()
        return prefRole?.replaceFirstChar { it.uppercase() } ?: "Pengguna"
    }

    private suspend fun fetchAndCacheRoles() {
        val rolesResponse = apiService.getRoles()
        val roleEntities = rolesResponse.data.map { it.toEntity() }
        roleDao.insertRoles(roleEntities)
    }

    private suspend fun updateUserPreferences(userDto: UserDetailDto) {
        userPreferences.saveUserName(userDto.name)
        if (!userDto.profilePictureUrl.isNullOrEmpty()) {
            userPreferences.saveUserAvatar(userDto.profilePictureUrl)
        }
    }
}