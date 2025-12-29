package com.dishut_lampung.sitanihut.domain.usecase.penyuluh

import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.repository.PenyuluhRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPenyuluhDetailUseCase @Inject constructor(
    private val repository: PenyuluhRepository
) {
    operator fun invoke(id: String): Flow<Resource<Penyuluh>> {
        return TODO()
    }
}