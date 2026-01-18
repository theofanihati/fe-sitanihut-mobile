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
        return dao.getAllUser(query).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun syncUserData(): Resource<Unit> {
        return try {
            val limitPerRequest = 50

            val pageOneResponse = apiService.getUserList(
                page = 1,
                limit = limitPerRequest,
                search = ""
            )
            if (pageOneResponse.statusCode == 200 && pageOneResponse.data != null) {
                val paginationData = pageOneResponse.data
                val allData = paginationData.data.toMutableList()
                val totalPages = paginationData.totalPages

                if (totalPages > 1) {
                    for (page in 2..totalPages) {
                        try {
                            val nextResponse = apiService.getUserList(
                                page = page,
                                limit = limitPerRequest,
                                search = ""
                            )
                            if (nextResponse.statusCode == 200 && nextResponse.data != null) {
                                allData.addAll(nextResponse.data.data)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                if (allData.isNotEmpty()) {
                    val entities = allData.map { it.toEntity() }
                    dao.updateData(entities)
                }
                Resource.Success(Unit)
            } else {
                Resource.Error(pageOneResponse.message ?: "Gagal sinkronisasi data Petani")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal sinkronisasi data User")
        }
    }

    override suspend fun deleteUser(id: String): Resource<Unit> {
        return try {
            val response = apiService.deleteUser(id)
            if (response.statusCode == 200 || response.statusCode == 204) {
                dao.deleteUser(id)
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menghapus data User")
        }
    }

    override fun getUserDetail(id: String): Flow<Resource<UserDetail>> = flow {
        emit(Resource.Loading())

        try {
            val localData = dao.getUserById(id).first()
            if (localData != null) {
                emit(Resource.Success(localData.toDomain()))
            }

            val response = apiService.getUserDetail(id)
            val remoteData = response.data

            val roleName = resolveRoleName(remoteData?.roleId ?: "")

            if (response.statusCode == 200 && remoteData != null) {
                val userEntity = remoteData.toEntity(roleName)
                dao.upsertAll(listOf(userEntity))
                emit(Resource.Success(userEntity.toDomain()))
            } else {
                if (localData == null) {
                    emit(Resource.Error(response.message ?: "Data tidak ditemukan"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat data"))
        }
    }

    override suspend fun createUser(input: CreateUserInput): Resource<Unit> {
        return try {
            val requestDto = input.toDto()
            val response = apiService.createUser(requestDto)

            val newData = response.data
            val roleName = resolveRoleName(newData.roleId)

            if (newData != null) {
                dao.upsertAll(listOf(newData.toEntity(roleName)))
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Gagal menambah user baru")
        }
    }

    override suspend fun updateUser(id: String, changes: Map<String, Any?>): Resource<Unit> {
        return try {
            apiService.updateUser(id, changes)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal update")
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
}