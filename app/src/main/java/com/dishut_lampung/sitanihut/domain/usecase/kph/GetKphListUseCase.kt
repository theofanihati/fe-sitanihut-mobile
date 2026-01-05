package com.dishut_lampung.sitanihut.domain.usecase.kph

import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.repository.KphRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetKphListUseCase @Inject constructor(
    private val repository: KphRepository
) {
    operator fun invoke(): Flow<List<Kph>> {
        return TODO()
    }
}