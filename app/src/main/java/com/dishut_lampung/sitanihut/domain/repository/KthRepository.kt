package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

interface KthRepository {
    fun getKthList(query: String): Flow<Resource<List<Kth>>>
    suspend fun deleteKth(id: String): Resource<Unit>
    suspend fun syncKthData(): Resource<Unit>
    fun getKthById(id: String): Flow<Kth?>
    suspend fun createKth(input: CreateKthInput): Resource<Unit>
    fun getKthDetail(id: String): Flow<Resource<Kth>>
    suspend fun updateKth(id: String, input: CreateKthInput): Resource<Unit>
}