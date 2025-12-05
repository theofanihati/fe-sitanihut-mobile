package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.util.Resource
import kotlinx.coroutines.flow.Flow

interface CommodityRepository {
    fun getCommodities(query: String = ""): Flow<Resource<List<Commodity>>>
}