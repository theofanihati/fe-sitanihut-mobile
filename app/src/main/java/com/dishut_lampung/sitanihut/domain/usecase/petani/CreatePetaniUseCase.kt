package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class CreatePetaniUseCase @Inject constructor(
    private val repository: PetaniRepository
) {
    suspend operator fun invoke(input: CreatePetaniInput): Resource<Unit> {
        return repository.createPetani(input)
    }
}