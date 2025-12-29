package com.dishut_lampung.sitanihut.domain.usecase.penyuluh

import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.repository.PenyuluhRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetPenyuluhUseCase @Inject constructor(
    private val repository: PenyuluhRepository
){
    operator fun invoke(userRole: String, query: String = ""): Flow<Resource<List<Penyuluh>>>{
        val isPj = userRole.equals("penanggung jawab", ignoreCase = true) ||
                userRole.equals("penanggung-jawab", ignoreCase = true)

        if (!isPj){
            return flow { emit(Resource.Error("Anda tidak memiliki akses")) }
        }
        return repository.getPenyuluhList(query)
    }
}