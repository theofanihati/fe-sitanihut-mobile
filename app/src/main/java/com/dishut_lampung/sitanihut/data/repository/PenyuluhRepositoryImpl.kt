package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.PenyuluhDao
import com.dishut_lampung.sitanihut.data.local.entity.PenyuluhEntity
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.remote.api.PenyuluhApiService
import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.repository.PenyuluhRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PenyuluhRepositoryImpl @Inject constructor(
    private val apiService: PenyuluhApiService,
    private val dao: PenyuluhDao
) : PenyuluhRepository {

    override fun getPenyuluhList(query: String): Flow<Resource<List<Penyuluh>>> {
        return dao.getAllPenyuluh(query).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override suspend fun syncPenyuluhData(): Resource<Unit> {
        return try {
            val response = apiService.getPenyuluhList(limit = 100)
            val items = response.data.data

            if (items.isNotEmpty()) {
                val entities = items.map { dto ->
                    PenyuluhEntity(
                        id = dto.id,
                        name = dto.name,
                        identityNumber = dto.identityNumber,
                        position = dto.position,
                        gender = dto.gender,
                        kphId = dto.kphId,
                        kphName = dto.kphName,
                        whatsAppNumber = dao.getPenyuluhById(dto.id)?.whatsAppNumber
                    )
                }
                dao.upsertAll(entities)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal sinkronisasi data penyuluh")
        }
    }

    override fun getPenyuluhDetail(id: String): Flow<Resource<Penyuluh>> {
        return flow {
            val localData = dao.getPenyuluhById(id)
            if (localData != null) {
                emit(Resource.Success(localData.toDomain()))
            } else {
                emit(Resource.Loading())
            }

            try {
                val response = apiService.getPenyuluhDetail(id)
                val dto = response.data

                val newEntity = PenyuluhEntity(
                    id = dto.id,
                    name = dto.name,
                    identityNumber = dto.identityNumber,
                    position = dto.position,
                    gender = dto.gender,
                    kphId = dto.kphId,
                    kphName = dto.kphName,
                    whatsAppNumber = dto.whatsAppNumber
                )
                dao.upsertAll(listOf(newEntity))

            } catch (e: Exception) {
//                Log.e("PenyuluhRepo", "Gagal fetch detail: ${e.message}")
                if (localData == null) {
                    emit(Resource.Error("Gagal memuat data penyuluh"))
                }
            }

            dao.getPenyuluhByIdFlow(id).collect { entity ->
                if (entity != null) {
                    emit(Resource.Success(entity.toDomain()))
                }
            }
        }
    }
}