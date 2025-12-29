package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

interface PenyuluhRepository {
    fun getPenyuluhList(query: String): Flow<Resource<List<Penyuluh>>>
    fun getPenyuluhDetail(id: String): Flow<Resource<Penyuluh>>
    suspend fun syncPenyuluhData(): Resource<Unit>
}