package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.PenyuluhDao
import com.dishut_lampung.sitanihut.data.local.entity.PenyuluhEntity
import com.dishut_lampung.sitanihut.data.remote.api.PenyuluhApiService
import com.dishut_lampung.sitanihut.data.remote.dto.CommodityDto
import com.dishut_lampung.sitanihut.data.remote.dto.PenyuluhListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PenyuluhRepositoryImplTest {

    private lateinit var apiService: PenyuluhApiService
    private lateinit var dao: PenyuluhDao
    private lateinit var repository: PenyuluhRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        dao = mockk(relaxed = true)
        repository = PenyuluhRepositoryImpl(apiService, dao)
    }

    @Test
    fun `getPenyuluhList should emit data from DAO`() = runTest {
        val dummyEntity = PenyuluhEntity("1", "Ani Sirani", "123", "Ahli", "Pria", "kph1", "KPH A")
        every { dao.getAllPenyuluh(any()) } returns flowOf(listOf(dummyEntity))

        val result = repository.getPenyuluhList("").first()
        assertTrue(result is Resource.Success)
        assertEquals(1, result.data?.size)
        assertEquals("Ani Sirani", result.data?.first()?.name)
    }

    @Test
    fun `getPenyuluhDetail should emit local data first then fetch network`() = runTest {
        val dummyEntity = PenyuluhEntity("1", "Ani Sirani", "123", "Ahli", "Pria", "kph1", "KPH A")
        coEvery { dao.getPenyuluhById("1") } returns dummyEntity
        every { dao.getPenyuluhByIdFlow("1") } returns flowOf(dummyEntity)
        coEvery { apiService.getPenyuluhDetail("1") } throws Exception("Network Error")

        val flow = repository.getPenyuluhDetail("1")
        flow.collect { result ->
            if (result is Resource.Success) {
                assertEquals("Ani Sirani", result.data?.name)
            }
        }

        coVerify { apiService.getPenyuluhDetail("1") }
    }

    @Test
    fun `syncPenyuluhData should fetch from API and save to DAO`() = runTest {
        val remoteItem = PenyuluhListItemDto("1", "Ahmad Remote", "123", "Ahli", "Pria", "kph1", "KPH A")
        val apiResponse = ApiResponse(
            statusCode = 200,
            message = "Success",
            data = PaginatedData(listOf(remoteItem), 1, 1)
        )

        coEvery { apiService.getPenyuluhList(search = "", page = 1, limit = 50) } returns apiResponse

        val result = repository.syncPenyuluhData()
        assertTrue(result is Resource.Success)
        coVerify { dao.updateData(any()) }
    }

    @Test
    fun `syncPenyuluhData when api returns empty should not insert`() = runTest {
        val paginatedData = PaginatedData(
            data = emptyList<PenyuluhListItemDto>(),
            totalPages = 0,
            count = 0
        )
        val apiResponse = ApiResponse(200, "success", paginatedData)
        coEvery { apiService.getPenyuluhList(search = "", page = 1, limit = 50) } returns apiResponse

        val result = repository.syncPenyuluhData()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 0) { dao.updateData(any()) }
    }

    @Test
    fun `syncPenyuluhData success should verify ALL fields mapping correctly`() = runTest {
        val dto = PenyuluhListItemDto("1", "Ahmad Remote", "123", "Ahli", "Pria", "kph1", "KPH A")
        val paginatedData = PaginatedData(
            data = listOf(dto),
            totalPages = 1,
            count = 1
        )
        val apiResponse = ApiResponse(200, "ok", paginatedData)
        coEvery { apiService.getPenyuluhList(search = "", page = 1, limit = 50) } returns apiResponse

        val result = repository.syncPenyuluhData()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { dao.updateData(
            match { list ->
                val entity = list.first()
                entity.id == "1" &&
                        entity.name == "Ahmad Remote" &&
                        entity.identityNumber == "123" &&
                        entity.position == "Ahli" &&
                        entity.gender == "Pria" &&
                        entity.kphId == "kph1" &&
                        entity.kphName == "KPH A"
            })
        }
    }

    @Test
    fun `syncPenyuluhData returns Success and inserts ALL pages when API has multiple pages`() = runTest {
        val itemPage1 = PenyuluhListItemDto("1", "Ahmad Remote", "123", "Ahli", "Pria", "kph1", "KPH A")
        val itemPage2 = PenyuluhListItemDto("2", "Anugerah Remote", "124", "Muda", "Pria", "kph1", "KPH A")

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

        coEvery { apiService.getPenyuluhList(search = "", page = 1, limit = 50) } returns responsePage1
        coEvery { apiService.getPenyuluhList(search = "", page = 2, limit = 50) } returns responsePage2

        val result = repository.syncPenyuluhData()
        assertTrue(result is Resource.Success)

        coVerify {
            dao.updateData(match { list ->
                list.size == 2 &&
                        list.any { it.id == "1" } &&
                        list.any { it.id == "2" }
            })
        }

        coVerify(exactly = 1) { apiService.getPenyuluhList(search = "", page = 1, limit = 50) }
        coVerify(exactly = 1) { apiService.getPenyuluhList(search = "", page = 2, limit = 50) }
    }

    @Test
    fun `syncPenyuluhData handles single page correctly`() = runTest {
        val itemPage1 = PenyuluhListItemDto("1", "Ahmad Remote", "123", "Ahli", "Pria", "kph1", "KPH A")
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 1, count = 1)
        val response = ApiResponse(200, "Success", page1Data)

        coEvery { apiService.getPenyuluhList(search = "", page = 1, limit = 50) } returns response

        val result = repository.syncPenyuluhData()

        assertTrue(result is Resource.Success)
        coVerify { dao.updateData(match { it.size == 1 }) }
        coVerify(exactly = 0) { apiService.getPenyuluhList(search = "", page = 2, limit = 50) }
    }

    @Test
    fun `syncPenyuluhData returns Error when Page 1 returns non-200`() = runTest {
        val errorResponse = ApiResponse<PaginatedData<PenyuluhListItemDto>>(
            statusCode = 400,
            message = "Bad Request",
            data = PaginatedData(emptyList(), 0, 0)
        )

        coEvery { apiService.getPenyuluhList(search = "", page = 1, limit = 50) } returns errorResponse
        val result = repository.syncPenyuluhData()
        assertTrue(result is Resource.Error)
        assertEquals("Bad Request", result.message)

        coVerify(exactly = 0) { dao.deleteAll() }
    }

    @Test
    fun `syncPenyuluhData returns Error when API throws exception on Page 1`() = runTest {
        val errorMessage = "Network Error"
        coEvery { apiService.getPenyuluhList(search = "", page = 1, limit = 50) } throws RuntimeException(errorMessage)

        val result = repository.syncPenyuluhData()
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `syncPenyuluhData continues and saves Page 1 data even if Page 2 fails`() = runTest {
        val itemPage1 = PenyuluhListItemDto("1", "Ahmad Remote", "123", "Ahli", "Pria", "kph1", "KPH A")
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 2, count = 2)

        coEvery { apiService.getPenyuluhList(search = "", page = 1, limit = 50) } returns ApiResponse(200, "OK", page1Data)
        coEvery { apiService.getPenyuluhList(search = "", page = 2, limit = 50) } throws RuntimeException("Timeout")

        val result = repository.syncPenyuluhData()
        assertTrue(result is Resource.Success)

        coVerify { dao.updateData(match { it.size == 1 && it[0].id == "1" }) }
    }
}