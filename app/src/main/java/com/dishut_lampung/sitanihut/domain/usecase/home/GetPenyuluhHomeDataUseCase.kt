package com.dishut_lampung.sitanihut.domain.usecase.home

import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPenyuluhHomeDataUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    operator fun invoke(): Flow<Resource<List<Report>>> {
        return repository.getReportsByStatus("menunggu")
    }
}