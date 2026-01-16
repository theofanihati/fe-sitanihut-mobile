package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class SyncPetaniDataUseCase @Inject constructor(
    private val repository: PetaniRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return repository.syncPetaniData()
    }
}