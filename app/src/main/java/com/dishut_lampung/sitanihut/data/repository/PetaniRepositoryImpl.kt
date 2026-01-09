package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.PetaniDao
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.mapper.toDto
import com.dishut_lampung.sitanihut.data.mapper.toEntity
import com.dishut_lampung.sitanihut.data.remote.api.PetaniApiService
import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PetaniRepositoryImpl @Inject constructor(
    private val apiService: PetaniApiService,
    private val dao: PetaniDao
): PetaniRepository {

    override fun getPetaniList(query: String): Flow<Resource<List<Petani>>> {
        return TODO()
    }

    override suspend fun syncPetaniData(): Resource<Unit> {
        return TODO()
    }

    override suspend fun deletePetani(id: String): Resource<Unit> {
        return TODO()
    }

    override fun getPetaniDetail(id: String): Flow<Resource<Petani>> = flow {
        TODO()
    }

    override suspend fun createPetani(input: CreatePetaniInput): Resource<Unit> {
        return TODO()
    }

    override suspend fun updatePetani(id: String, input: CreatePetaniInput): Resource<Unit> {
        return TODO()
    }
}