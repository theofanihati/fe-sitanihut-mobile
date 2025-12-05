package com.dishut_lampung.sitanihut.domain.usecase.commodity

import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.domain.repository.CommodityRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCommoditiesUseCaseTest {
    private val repository: CommodityRepository = mockk()
    private val useCase = GetCommoditiesUseCase(repository)

    @Test
    fun `invoke should call repository getCommodities with correct query`() = runTest {
        val query = "Jagung"
        val dummyData = listOf(
            Commodity("1", "JG-01","Jagung Manis", "buah buahan"),
            Commodity("2","JG-02", "Jagung Bakar", "buah buahan")
        )
        coEvery { repository.getCommodities(query) } returns flowOf(Resource.Success(dummyData))

        val resultFlow = useCase(query)
        val result = resultFlow.first()
        coVerify(exactly = 1) { repository.getCommodities(query) }
        assert(result is Resource.Success)
        assertEquals(dummyData, (result as Resource.Success).data)
    }

    @Test
    fun `invoke should call repository with empty string when query is null or empty`() = runTest {
        val emptyQuery = ""
        coEvery { repository.getCommodities(emptyQuery) } returns flowOf(Resource.Success(emptyList()))

        useCase()
        coVerify(exactly = 1) { repository.getCommodities(emptyQuery) }
    }
}