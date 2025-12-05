package com.dishut_lampung.sitanihut.domain.usecase.commodity

import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.domain.repository.CommodityRepository
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

class GetCommoditiesUseCase(
    private val repository: CommodityRepository
) {
    operator fun invoke(query: String = ""): Flow<Resource<List<Commodity>>> {
        return TODO("blum we wkwkwk")
    }
}