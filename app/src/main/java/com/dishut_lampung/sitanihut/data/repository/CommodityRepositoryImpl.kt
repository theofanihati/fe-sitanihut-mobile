package com.dishut_lampung.sitanihut.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.dao.CommodityDao
import com.dishut_lampung.sitanihut.data.local.entity.CommodityEntity
import com.dishut_lampung.sitanihut.data.mapper.toDbValue
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.CommodityApiService
import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.repository.CommodityRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class CommodityRepositoryImpl(
    private val apiService: CommodityApiService,
    private val dao: CommodityDao
) : CommodityRepository {

    override fun getCommodities(params: String): Flow<Resource<List<Commodity>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getCommodities(search = params)
            val dtoList = response.data.data

            if (dtoList.isNotEmpty()) {
                val entities = dtoList.map { dto ->
                    CommodityEntity(
                        id = dto.id,
                        code = dto.code,
                        name = dto.name,
                        category = dto.category
                    )
                }
                dao.insertCommodities(entities)
            }
        } catch (e: Exception) {
            emit(Resource.Error("Gagal update data: ${e.message}"))
        }
        val localData = dao.getCommodities(params)

        localData.collect { entities ->
            val domainData = entities.map { entity ->
                Commodity(
                    id = entity.id,
                    code = entity.code,
                    name = entity.name,
                    category = entity.category
                )
            }
            emit(Resource.Success(domainData))
        }
    }
}