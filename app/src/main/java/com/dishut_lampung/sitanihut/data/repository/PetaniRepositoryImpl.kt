package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.PetaniDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toDto
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.PetaniApiService
import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PetaniRepositoryImpl @Inject constructor(
    private val apiService: PetaniApiService,
    private val dao: PetaniDao
): PetaniRepository {

    override fun getPetaniList(query: String): Flow<Resource<List<Petani>>> {
        return dao.getAllPetani(query).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun syncPetaniData(): Resource<Unit> {
        return try {
            val response = apiService.getPetaniList(limit = 50)
            val items = response.data?.data ?: emptyList()

            if (items.isNotEmpty()) {
                val entities = items.map { it.toEntity() }
                dao.upsertAll(entities)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal sinkronisasi data Petani")
        }
    }

    override suspend fun deletePetani(id: String): Resource<Unit> {
        return try {
            val response = apiService.deletePetani(id)
            if (response.statusCode == 200 || response.statusCode == 204) {
                dao.deletePetani(id)
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menghapus data Petani")
        }
    }

    override fun getPetaniDetail(id: String): Flow<Resource<Petani>> = flow {
        emit(Resource.Loading())

        try {
            val localData = dao.getPetaniById(id).first()
            if (localData != null) {
                emit(Resource.Success(localData.toDomain()))
            }

            val response = apiService.getPetaniDetail(id)
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

    override suspend fun createPetani(input: CreatePetaniInput): Resource<Unit> {
        return try {
            val requestDto = input.toDto()
            val response = apiService.createPetani(requestDto)

            val newData = response.data
            if (newData != null) {
                dao.upsertAll(listOf(newData.toEntity()))
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.localizedMessage ?: "Gagal menambah petani baru")
        }
    }

    override suspend fun updatePetani(id: String, changes: Map<String, Any?>): Resource<Unit> {
        return try {
            apiService.updatePetani(id, changes)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal update")
        }
    }
}