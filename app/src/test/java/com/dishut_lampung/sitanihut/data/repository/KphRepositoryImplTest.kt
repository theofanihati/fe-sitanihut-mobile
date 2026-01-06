package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.KphDao
import com.dishut_lampung.sitanihut.data.local.entity.KphEntity
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.remote.api.KphApiService
import com.dishut_lampung.sitanihut.data.remote.dto.KphDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
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
    fun `syncKphData returns Success when API call is successful`() = runTest {
        val kphDto = KphDto(id = "1", name = "KPH Batutegi")
        val apiResponse = ApiResponse(
            statusCode = 200,
            message = "Success",
            data = listOf(kphDto)
        )

        coEvery { apiService.getKphList() } returns apiResponse

        val result = repository.syncKphData()
        assertTrue(result is Resource.Success)

        coVerify {
            apiService.getKphList()
            dao.deleteAll()
            dao.insertAll(any())
        }
    }

    @Test
    fun `syncKphData returns Error when API returns non-200`() = runTest {
        val apiResponse = ApiResponse<List<KphDto>>(
            statusCode = 400,
            message = "Bad Request",
            data = emptyList()
        )

        coEvery { apiService.getKphList() } returns apiResponse

        val result = repository.syncKphData()
        assertTrue(result is Resource.Error)
        assertEquals("Bad Request", result.message)

        coVerify(exactly = 0) { dao.deleteAll() }
        coVerify(exactly = 0) { dao.insertAll(any()) }
    }

    @Test
    fun `syncKphData returns Error when API throws exception`() = runTest {
        val errorMessage = "Network Error"
        coEvery { apiService.getKphList() } throws RuntimeException(errorMessage)

        val result = repository.syncKphData()
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)

        coVerify(exactly = 0) { dao.deleteAll() }
    }
}