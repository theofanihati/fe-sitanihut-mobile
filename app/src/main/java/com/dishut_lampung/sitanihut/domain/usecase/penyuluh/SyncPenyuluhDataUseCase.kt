package com.dishut_lampung.sitanihut.domain.usecase.penyuluh

import com.dishut_lampung.sitanihut.domain.repository.PenyuluhRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class SyncPenyuluhDataUseCase @Inject constructor(
    private val repository: PenyuluhRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return repository.syncPenyuluhData()
    }
}