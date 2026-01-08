package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.repository.KthRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class UpdateKthUseCase @Inject constructor(
    private val repository: KthRepository
) {
    suspend operator fun invoke(id: String, input: CreateKthInput): Resource<Unit> {
        return repository.updateKth(id, input)
    }
}