package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.KphDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.KphApiService
import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.repository.KphRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class KphRepositoryImpl @Inject constructor(
    private val apiService: KphApiService,
    private val dao: KphDao
) : KphRepository {

    override fun getKphList(): Flow<List<Kph>> {
        return TODO()
    }

    override suspend fun syncKphData(): Resource<Unit> {
        return TODO()
    }
}