package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.KphDao
import com.dishut_lampung.sitanihut.data.local.entity.KphEntity
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.remote.api.KphApiService
import com.dishut_lampung.sitanihut.data.remote.dto.KphDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KphRepositoryImplTest {

    private lateinit var repository: KphRepositoryImpl
    private val apiService: KphApiService = mockk()
    private val dao: KphDao = mockk(relaxed = true)

    @Before
    fun setUp() {
        repository = KphRepositoryImpl(apiService, dao)
    }

    @Test
    fun `getKphList emits list of Kph from DAO`() = runTest {
        val kphEntity = KphEntity(id = "1", name = "KPH Batutegi")
        val expectedDomainList = listOf(kphEntity.toDomain())

        every { dao.getAllKph() } returns flowOf(listOf(kphEntity))

        val result = repository.getKphList().first()
        assertEquals(expectedDomainList.size, result.size)
        assertEquals(expectedDomainList[0].name, result[0].name)
        verify { dao.getAllKph() }
    }

    @Test
    fun `syncKphData returns Success and inserts ALL pages when API has multiple pages`() = runTest {
        val itemPage1 = KphDto("1", "KPH 1")
        val itemPage2 = KphDto("2", "KPH 2")

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

        coEvery { apiService.getKphList(page = 1) } returns responsePage1
        coEvery { apiService.getKphList(page = 2) } returns responsePage2

        val result = repository.syncKphData()
        assertTrue(result is Resource.Success)

        coVerify {
            dao.deleteAll()
            dao.insertAll(match { list ->
                list.size == 2 &&
                        list.any { it.id == "1" } &&
                        list.any { it.id == "2" }
            })
        }

        coVerify(exactly = 1) { apiService.getKphList(page = 1) }
        coVerify(exactly = 1) { apiService.getKphList(page = 2) }
    }

    @Test
    fun `syncKphData handles single page correctly`() = runTest {
        val itemPage1 = KphDto("1", "KPH 1")
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 1, count = 1)
        val response = ApiResponse(200, "Success", page1Data)

        coEvery { apiService.getKphList(page = 1) } returns response

        val result = repository.syncKphData()

        assertTrue(result is Resource.Success)
        coVerify { dao.insertAll(match { it.size == 1 }) }
        coVerify(exactly = 0) { apiService.getKphList(page = 2) }
    }

    @Test
    fun `syncKphData returns Error when Page 1 returns non-200`() = runTest {
        val errorResponse = ApiResponse<PaginatedData<KphDto>>(
            statusCode = 400,
            message = "Bad Request",
            data = PaginatedData(emptyList(), 0, 0)
        )

        coEvery { apiService.getKphList(page = 1) } returns errorResponse
        val result = repository.syncKphData()
        assertTrue(result is Resource.Error)
        assertEquals("Bad Request", result.message)

        coVerify(exactly = 0) { dao.deleteAll() }
    }

    @Test
    fun `syncKphData returns Error when API throws exception on Page 1`() = runTest {
        val errorMessage = "Network Error"
        coEvery { apiService.getKphList(page = 1) } throws RuntimeException(errorMessage)

        val result = repository.syncKphData()
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `syncKphData continues and saves Page 1 data even if Page 2 fails`() = runTest {
        val itemPage1 = KphDto("1", "KPH 1")
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 2, count = 2)

        coEvery { apiService.getKphList(page = 1) } returns ApiResponse(200, "OK", page1Data)
        coEvery { apiService.getKphList(page = 2) } throws RuntimeException("Timeout")

        val result = repository.syncKphData()
        assertTrue(result is Resource.Success)

        coVerify { dao.insertAll(match { it.size == 1 && it[0].id == "1" }) }
    }
}