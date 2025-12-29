package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.PenyuluhDao
import com.dishut_lampung.sitanihut.data.local.entity.PenyuluhEntity
import com.dishut_lampung.sitanihut.data.remote.api.PenyuluhApiService
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
    fun `syncPenyuluhData should fetch from API and save to DAO`() = runTest {
        val remoteItem = PenyuluhListItemDto("1", "Ahmad Remote", "123", "Ahli", "Pria", "kph1", "KPH A")
        val apiResponse = ApiResponse(
            statusCode = 200,
            message = "Success",
            data = PaginatedData(listOf(remoteItem), 1, 1)
        )

        coEvery { apiService.getPenyuluhList(any(), any()) } returns apiResponse

        val result = repository.syncPenyuluhData()
        assertTrue(result is Resource.Success)
        coVerify { dao.upsertAll(any()) }
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
}