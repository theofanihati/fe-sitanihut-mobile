package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

interface KphRepository {
    fun getKphList(): Flow<List<Kph>>
    suspend fun syncKphData(): Resource<Unit>
}