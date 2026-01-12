package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPetaniDetailUseCase @Inject constructor(
    private val repository: PetaniRepository
) {
    operator fun invoke(id: String): Flow<Resource<Petani>> {
        return TODO()
    }
}