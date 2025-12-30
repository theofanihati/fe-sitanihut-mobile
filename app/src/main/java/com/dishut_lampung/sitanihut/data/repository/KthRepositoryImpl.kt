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
        return TODO()
    }

    override suspend fun syncKthData(): Resource<Unit> {
        return TODO()
    }

    override suspend fun deleteKth(id: String): Resource<Unit> {
        return TODO()
    }
}