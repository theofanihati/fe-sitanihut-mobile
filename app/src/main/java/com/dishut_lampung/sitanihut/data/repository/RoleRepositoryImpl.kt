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
        emit(Resource.Loading())
        val localFlow = dao.getAllRoles().map { entities ->
            val domainData = entities.map { it.toDomain() }
            Resource.Success(domainData)
        }
        emitAll(localFlow)
    }

    override suspend fun syncRoleData(): Resource<Unit> {
        return try {
            val pageOneResponse = api.getRoles(page = 1)

            if (pageOneResponse.statusCode == 200 && pageOneResponse.data != null) {
                val paginationData = pageOneResponse.data
                val allRoleData = paginationData.data.toMutableList()
                val totalPages = paginationData.totalPages

                if (totalPages > 1) {
                    for (page in 2..totalPages) {
                        try {
                            val nextResponse = api.getRoles(page = page)
                            if (nextResponse.statusCode == 200 && nextResponse.data != null) {
                                allRoleData.addAll(nextResponse.data.data)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                if (allRoleData.isNotEmpty()) {
                    val roleEntities = allRoleData.map { it.toEntity() }

                    dao.updateData(roleEntities)
//                    android.util.Log.d("REPO_ROLE", "Total data tersimpan: ${roleEntities.size}")
                    Resource.Success(Unit)
                } else {
                    Resource.Success(Unit)
                }
            } else {
                Resource.Error(pageOneResponse.message ?: "Gagal mengambil data role")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan koneksi")
        }
    }
}