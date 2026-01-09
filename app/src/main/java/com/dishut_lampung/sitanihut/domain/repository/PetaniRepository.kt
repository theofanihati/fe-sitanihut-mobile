package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

interface PetaniRepository {
    fun getPetaniList(query: String): Flow<Resource<List<Petani>>>
    suspend fun deletePetani(id: String): Resource<Unit>
    suspend fun syncPetaniData(): Resource<Unit>
    fun getPetaniDetail(id: String): Flow<Resource<Petani>>
    suspend fun createPetani(input: CreatePetaniInput): Resource<Unit>
    suspend fun updatePetani(id: String, input: CreatePetaniInput): Resource<Unit>
}