package com.dishut_lampung.sitanihut.data.repository

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.dao.CommodityDao
import com.dishut_lampung.sitanihut.data.local.entity.CommodityEntity
import com.dishut_lampung.sitanihut.data.remote.api.CommodityApiService
import com.dishut_lampung.sitanihut.data.remote.dto.CommodityDto
import com.dishut_lampung.sitanihut.data.remote.dto.KphDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.*
import kotlinx.coroutines.flow.flow
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

    private val commodityEntity = CommodityEntity(
        id = "1",
        code = "JG01",
        name = "Jagung Manis",
        category = "Hortikultura"
    )

    private val commodityDto = CommodityDto(
        id = "1",
        code = "JG01",
        name = "Jagung Manis",
        category = "Hortikultura",
        createdAt = "2023-01-01"
    )

    @Before
    fun setUp() {
        mockApiService = mockk()
        mockDao = mockk(relaxed = true)
        repository = CommodityRepositoryImpl(mockApiService, mockDao)
    }

    @Test
    fun `getCommodities success should emit loading then success`() = runTest {
        val query = "Jagung"
        every { mockDao.getCommodities(query) } returns flowOf(listOf(commodityEntity))
        repository.getCommodities(query).test {
            assertTrue(awaitItem() is Resource.Loading)

            val successItem = awaitItem()
            assertTrue(successItem is Resource.Success)
            val data = successItem.data
            assertEquals(1, data?.size)
            assertEquals("Jagung Manis", data?.first()?.name)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `getCommodities should emit error when dao throws exception`() = runTest {
        val query = "Jagung"
        every { mockDao.getCommodities(query) } returns flow { throw Exception("DB Error") }

        repository.getCommodities(query).test {
            assertTrue(awaitItem() is Resource.Loading)
            val errorItem = awaitItem()
            assertTrue(errorItem is Resource.Error)
            assertTrue(errorItem.message!!.contains("Terjadi kesalahan database"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCommodities success with empty list should emit success with empty data`() = runTest {
        val query = "Hantu"
        every { mockDao.getCommodities(query) } returns flowOf(emptyList())

        repository.getCommodities(query).test {
            assertTrue(awaitItem() is Resource.Loading)
            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertTrue(result.data!!.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getCommodities should filter out duplicate emissions (distinctUntilChanged)`() = runTest {
        val query = "Jagung"
        val flowFromDao = flow {
            emit(listOf(commodityEntity))
            emit(listOf(commodityEntity))
        }
        every { mockDao.getCommodities(query) } returns flowFromDao

        repository.getCommodities(query).test {
            assertTrue(awaitItem() is Resource.Loading)

            val item1 = awaitItem()
            assertTrue(item1 is Resource.Success)
            awaitComplete()
        }
    }

    @Test
    fun `syncCommodities success should fetch api and insert to db`() = runTest {
        val paginatedData = PaginatedData(
            data = listOf(commodityDto),
            totalPages = 1,
            count = 1
        )
        val apiResponse = ApiResponse(200, "success", paginatedData)

        coEvery { mockApiService.getCommodities(search = "", page = 1) } returns apiResponse
        coJustRun { mockDao.updateData(any()) }

        val result = repository.syncCommodities()

        assertTrue(result is Resource.Success)

        coVerify { mockApiService.getCommodities(search = "", page = 1) }
        coVerify {
            mockDao.updateData(match { list ->
                list.size == 1 && list[0].id == "1"
            })
        }
    }

    @Test
    fun `syncCommodities when api returns empty should not insert`() = runTest {
        val paginatedData = PaginatedData(
            data = emptyList<CommodityDto>(),
            totalPages = 0,
            count = 0
        )
        val apiResponse = ApiResponse(200, "success", paginatedData)
        coEvery { mockApiService.getCommodities(search = "", page = 1) } returns apiResponse

        val result = repository.syncCommodities()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 0) { mockDao.updateData(any()) }
    }

    @Test
    fun `syncCommodities success should verify ALL fields mapping correctly`() = runTest {
        val dto = CommodityDto("1", "C01", "Jagung", "Sayur", "2023")
        val paginatedData = PaginatedData(
            data = listOf(dto),
            totalPages = 1,
            count = 1
        )
        val apiResponse = ApiResponse(200, "ok", paginatedData)
        coEvery { mockApiService.getCommodities(search = "", page = 1) } returns apiResponse

        val result = repository.syncCommodities()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { mockDao.updateData(
            match { list ->
                val entity = list.first()
                entity.id == "1" &&
                        entity.code == "C01" &&
                        entity.name == "Jagung" &&
                        entity.category == "Sayur"
            })
        }
    }

    @Test
    fun `syncCommodities returns Success and inserts ALL pages when API has multiple pages`() = runTest {
        val itemPage1 = CommodityDto("1", "JG-1", "Jagung Manis", "Buah buahan","09-09-2025")
        val itemPage2 = CommodityDto("2", "JG-1", "Jagung Ketan", "Buah buahan","09-09-2025")

        val page1Data = PaginatedData(
            data = listOf(itemPage1),
            totalPages = 2,
            count = 2
        )
        val responsePage1 = ApiResponse(200, "Success", page1Data)

        val page2Data = PaginatedData(
            data = listOf(itemPage2),
            totalPages = 2,
            count = 2
        )
        val responsePage2 = ApiResponse(200, "Success", page2Data)

        coEvery { mockApiService.getCommodities(search = "", page = 1) } returns responsePage1
        coEvery { mockApiService.getCommodities(search = "", page = 2) } returns responsePage2

        val result = repository.syncCommodities()
        assertTrue(result is Resource.Success)

        coVerify {
            mockDao.updateData(match { list ->
                list.size == 2 &&
                        list.any { it.id == "1" } &&
                        list.any { it.id == "2" }
            })
        }

        coVerify(exactly = 1) { mockApiService.getCommodities(search = "", page = 1) }
        coVerify(exactly = 1) { mockApiService.getCommodities(search = "", page = 2) }
    }

    @Test
    fun `syncCommodities handles single page correctly`() = runTest {
        val itemPage1 = CommodityDto("1", "JG-1", "Jagung Manis", "Buah buahan","09-09-2025")
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 1, count = 1)
        val response = ApiResponse(200, "Success", page1Data)

        coEvery { mockApiService.getCommodities(search = "", page = 1) } returns response

        val result = repository.syncCommodities()

        assertTrue(result is Resource.Success)
        coVerify { mockDao.updateData(match { it.size == 1 }) }
        coVerify(exactly = 0) { mockApiService.getCommodities(search = "", page = 2) }
    }

    @Test
    fun `syncCommodities returns Error when Page 1 returns non-200`() = runTest {
        val errorResponse = ApiResponse<PaginatedData<CommodityDto>>(
            statusCode = 400,
            message = "Bad Request",
            data = PaginatedData(emptyList(), 0, 0)
        )

        coEvery { mockApiService.getCommodities(search = "", page = 1) } returns errorResponse
        val result = repository.syncCommodities()
        assertTrue(result is Resource.Error)
        assertEquals("Bad Request", result.message)

        coVerify(exactly = 0) { mockDao.deleteAll() }
    }

    @Test
    fun `syncCommodities returns Error when API throws exception on Page 1`() = runTest {
        val errorMessage = "Network Error"
        coEvery { mockApiService.getCommodities(search = "", page = 1) } throws RuntimeException(errorMessage)

        val result = repository.syncCommodities()
        assertTrue(result is Resource.Error)
        assertEquals("Gagal sinkronisasi: $errorMessage", result.message)
    }

    @Test
    fun `syncCommodities continues and saves Page 1 data even if Page 2 fails`() = runTest {
        val itemPage1 = CommodityDto("1", "JG-1", "Jagung Manis", "Buah buahan","09-09-2025")
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 2, count = 2)

        coEvery { mockApiService.getCommodities(search = "", page = 1) } returns ApiResponse(200, "OK", page1Data)
        coEvery { mockApiService.getCommodities(search = "", page = 2) } throws RuntimeException("Timeout")

        val result = repository.syncCommodities()
        assertTrue(result is Resource.Success)

        coVerify { mockDao.updateData(match { it.size == 1 && it[0].id == "1" }) }
    }
}