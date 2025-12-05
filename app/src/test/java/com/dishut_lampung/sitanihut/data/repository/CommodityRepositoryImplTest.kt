package com.dishut_lampung.sitanihut.data.repository

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.dao.CommodityDao
import com.dishut_lampung.sitanihut.data.local.entity.CommodityEntity
import com.dishut_lampung.sitanihut.data.remote.api.CommodityApiService
import com.dishut_lampung.sitanihut.data.remote.dto.CommodityDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CommodityRepositoryImplTest {

    private lateinit var mockApiService: CommodityApiService
    private lateinit var mockDao: CommodityDao
    private lateinit var repository: CommodityRepositoryImpl

    @Before
    fun setUp() {
        mockApiService = mockk()
        mockDao = mockk(relaxed = true)
        repository = CommodityRepositoryImpl(mockApiService, mockDao)
    }

    @Test
    fun `getCommodities success from network should save to db and emit success`() = runTest {
        val query = "Jagung"
        val dto = CommodityDto(
            id = "1",
            code = "JG01",
            name = "Jagung Manis",
            category = "Hortikultura",
            createdAt = "2023-01-01"
        )

        val paginatedData = PaginatedData(
            data = listOf(dto),
            totalPages = 1,
            count = 1
        )

        val entity = CommodityEntity(
            id = "1",
            code = "JG01",
            name = "Jagung Manis",
            category = "buah buahan"
        )

        coEvery { mockApiService.getCommodities(search = query) } returns paginatedData
        every { mockDao.getCommodities(query) } returns flowOf(listOf(entity))

        repository.getCommodities(query).test {
            assertTrue(awaitItem() is Resource.Loading)
            val successItem = awaitItem()
            assertTrue(successItem is Resource.Success)

            val data = successItem.data
            assertEquals(1, data?.size)
            assertEquals("Jagung Manis", data?.first()?.name)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { mockApiService.getCommodities(search = query) }
        coVerify(exactly = 1) { mockDao.insertCommodities(any()) }
    }

    @Test
    fun `getCommodities fail from network should emit error then emit data from cache`() = runTest {
        val query = "Jagung"
        coEvery { mockApiService.getCommodities(search = query) } throws IOException("No Internet")

        val entity = CommodityEntity("1", "JG01", "Jagung", "Palawija")
        every { mockDao.getCommodities(query) } returns flowOf(listOf(entity))

        repository.getCommodities(query).test {
            assertTrue(awaitItem() is Resource.Loading)
            val errorItem = awaitItem()
            assertTrue(errorItem is Resource.Error)
            assertEquals("Gagal update data: No Internet", errorItem.message)

            val successItem = awaitItem()
            assertTrue(successItem is Resource.Success)
            assertEquals("Jagung", successItem.data?.first()?.name)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { mockApiService.getCommodities(search = query) }
        coVerify(exactly = 0) { mockDao.insertCommodities(any()) }
    }
}