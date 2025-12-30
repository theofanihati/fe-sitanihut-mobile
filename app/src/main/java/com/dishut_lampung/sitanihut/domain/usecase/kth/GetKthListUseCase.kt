package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.repository.KthRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetKthListUseCase @Inject constructor(
    private val repository: KthRepository
) {
    operator fun invoke(role: String, query: String = ""): Flow<Resource<List<Kth>>> {
        val isAllowed = role == "penyuluh" || role == "penanggung jawab"
        if (!isAllowed) {
            return flow {
                emit(Resource.Error("Anda tidak memiliki akses untuk melihat data KTH."))
            }
        }
        return repository.getKthList(query)
    }
}