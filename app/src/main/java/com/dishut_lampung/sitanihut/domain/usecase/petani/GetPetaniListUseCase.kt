package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetPetaniListUseCase @Inject constructor(
    private val repository: PetaniRepository
){
    operator fun invoke(role: String, query: String = ""): Flow<Resource<List<Petani>>> {
        return TODO()
    }
}