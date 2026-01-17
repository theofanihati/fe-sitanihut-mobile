package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.KthDao
import com.dishut_lampung.sitanihut.data.local.entity.KthEntity
import com.dishut_lampung.sitanihut.data.local.entity.PenyuluhEntity
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toDto
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.KthApiService
import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.repository.KthRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

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
            val limitPerRequest = 50

            val pageOneResponse = apiService.getKthList(
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
                            val nextResponse = apiService.getKthList(
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
                    val entities = allData.map { it.toEntity()
                    }
                    dao.updateData(entities)
                }
                Resource.Success(Unit)
            } else {
                Resource.Error(pageOneResponse.message ?: "Gagal sinkronisasi data penyuluh")
            }
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

    override fun getKthDetail(id: String): Flow<Resource<Kth>> = flow {
        emit(Resource.Loading())

        try {
            val localData = dao.getKthById(id).first()
            if (localData != null) {
                emit(Resource.Success(localData.toDomain()))
            }

            val response = apiService.getKthDetail(id)
            val remoteData = response.data

            if (response.statusCode == 200 && remoteData != null) {
                dao.upsertAll(listOf(remoteData.toEntity()))
                emit(Resource.Success(remoteData.toDomain()))
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

    override suspend fun createKth(input: CreateKthInput): Resource<Unit> {
        return try {
            val requestDto = input.toDto()
            val response = apiService.createKth(requestDto)

            val newData = response.data
            if (newData != null) {
                dao.upsertAll(listOf(newData.toEntity()))
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Gagal membuat KTH")
        }
    }

    override suspend fun updateKth(id: String, input: CreateKthInput): Resource<Unit> {
        return try {
            val requestDto = input.toDto()
            val response = apiService.updateKth(id, requestDto)

            if (response.statusCode == 200) {
                // val updatedData = response.data
                // if (updatedData != null) dao.upsertAll(listOf(updatedData.toEntity()))
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message ?: "Gagal mengupdate data")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan")
        }
    }
}