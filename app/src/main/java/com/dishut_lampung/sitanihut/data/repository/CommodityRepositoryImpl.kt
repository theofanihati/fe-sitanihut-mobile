package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.CommodityDao
import com.dishut_lampung.sitanihut.data.local.entity.CommodityEntity
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.remote.api.CommodityApiService
import com.dishut_lampung.sitanihut.data.remote.dto.CommodityDto
import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.domain.repository.CommodityRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class CommodityRepositoryImpl(
    private val apiService: CommodityApiService,
    private val dao: CommodityDao
) : CommodityRepository {

    override fun getCommodities(params: String): Flow<Resource<List<Commodity>>> =
        dao.getCommodities(params)
            .distinctUntilChanged()
            .map { entities ->
                val domainData = entities.map { it.toDomain() }
                Resource.Success(domainData) as Resource<List<Commodity>>
            }
            .onStart {
                emit(Resource.Loading())
            }
            .catch { e ->
                emit(Resource.Error("Terjadi kesalahan database: ${e.message}"))
            }

    override suspend fun syncCommodities(): Resource<Unit> {
        return try {
            val response = apiService.getCommodities(search = "")
            val dtoList = response.data.data

            if (!dtoList.isNullOrEmpty()) {
                saveCommoditiesToLocal(dtoList)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Gagal sinkronisasi: ${e.message}")
        }
    }

    private suspend fun saveCommoditiesToLocal(dtos: List<CommodityDto>) {
        val entities = dtos.map { dto ->
            CommodityEntity(
                id = dto.id,
                code = dto.code,
                name = dto.name,
                category = dto.category
            )
        }
        dao.insertCommodities(entities)
    }
}