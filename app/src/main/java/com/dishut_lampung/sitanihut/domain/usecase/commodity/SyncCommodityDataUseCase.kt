package com.dishut_lampung.sitanihut.domain.usecase.commodity

import com.dishut_lampung.sitanihut.domain.repository.CommodityRepository
import com.dishut_lampung.sitanihut.util.Resource
import javax.inject.Inject

class SyncCommodityDataUseCase @Inject constructor(
    private val repository: CommodityRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        return repository.syncCommodities()
    }
}