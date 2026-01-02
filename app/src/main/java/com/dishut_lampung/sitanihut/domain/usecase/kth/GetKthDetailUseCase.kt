package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.repository.KthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetKthDetailUseCase @Inject constructor(
    private val repository: KthRepository
) {
    operator fun invoke(id: String): Flow<Kth?> {
        return TODO()
    }
}