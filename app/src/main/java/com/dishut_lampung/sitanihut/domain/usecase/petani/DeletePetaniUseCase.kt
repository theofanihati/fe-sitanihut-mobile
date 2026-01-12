package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class DeletePetaniUseCase @Inject constructor(
    private val repository: PetaniRepository
) {
    suspend operator fun invoke(id: String): Resource<Unit> {
        return TODO()
    }
}