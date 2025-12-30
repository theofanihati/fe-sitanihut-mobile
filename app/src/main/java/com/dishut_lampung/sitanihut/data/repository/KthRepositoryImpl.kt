package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.KthDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.KthApiService
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.repository.KthRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class KthRepositoryImpl @Inject constructor(
    private val apiService: KthApiService,
    private val dao: KthDao
) : KthRepository {

    override fun getKthList(query: String): Flow<Resource<List<Kth>>> {
        return dao.getAllKth(query).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun syncKthData(): Resource<Unit> {
        return try {
            val response = apiService.getKthList(limit = 100)
            val items = response.data?.data ?: emptyList()

            if (items.isNotEmpty()) {
                val entities = items.map { it.toEntity() }
                dao.upsertAll(entities)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal sinkronisasi data KTH")
        }
    }

    override suspend fun deleteKth(id: String): Resource<Unit> {
        return try {
            val response = apiService.deleteKth(id)
            if (response.statusCode == 200 || response.statusCode == 204) {
                dao.deleteKth(id)
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menghapus data KTH")
        }
    }
}