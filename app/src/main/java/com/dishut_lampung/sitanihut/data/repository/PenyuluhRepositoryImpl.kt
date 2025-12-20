package com.dishut_lampung.sitanihut.data.repository

import android.util.Log
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

    override fun getPenyuluhList(): Flow<Resource<List<Penyuluh>>> {
        return TODO()
    }

    override suspend fun syncPenyuluhData(): Resource<Unit> {
        return TODO()
    }

    override fun getPenyuluhDetail(id: String): Flow<Resource<Penyuluh>> {
        return TODO()
    }
}