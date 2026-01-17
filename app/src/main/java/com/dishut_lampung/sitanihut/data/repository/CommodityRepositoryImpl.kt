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
            val pageOneResponse = apiService.getCommodities(search = "", page = 1)

            if (pageOneResponse.statusCode == 200 && pageOneResponse.data != null) {
                val paginationData = pageOneResponse.data
                val allCommodities = paginationData.data.toMutableList()
                val totalPages = paginationData.totalPages

                if (totalPages > 1) {
                    for (page in 2..totalPages) {
                        try{
                            val nextResponse = apiService.getCommodities(search = "", page = page)
                            if (nextResponse.statusCode == 200 && nextResponse.data != null) {
                                allCommodities.addAll(nextResponse.data.data)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                if (allCommodities.isNotEmpty()) {
                    val entities = allCommodities.map { dto ->
                        CommodityEntity(
                            id = dto.id,
                            code = dto.code,
                            name = dto.name,
                            category = dto.category
                        )
                    }
                    dao.updateData(entities)
                }
                Resource.Success(Unit)
            } else {
                Resource.Error(pageOneResponse.message ?: "Gagal")
            }
        } catch (e: Exception) {
            Resource.Error("Gagal sinkronisasi: ${e.message}")
        }
    }
}