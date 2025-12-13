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
        return userDao.getUserById(userId).map { userEntity ->
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

            if (roleName == null) {
                val prefRole = userPreferences.userRole.first()
                roleName = prefRole?.replaceFirstChar { it.uppercase() }
            }
            val userEntity = userDto.toEntity(roleName ?: "Pengguna")

            userDao.upsertUser(userEntity)

            userPreferences.saveUserName(userDto.name)
            if (!userDto.profilePictureUrl.isNullOrEmpty()) {
                userPreferences.saveUserAvatar(userDto.profilePictureUrl)
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Gagal sinkronisasi: ${e.message}")
        }
    }
}