package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.KthDao
import com.dishut_lampung.sitanihut.data.local.entity.KthEntity
import com.dishut_lampung.sitanihut.data.remote.api.KthApiService
import com.dishut_lampung.sitanihut.data.remote.dto.KthListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KthRepositoryImplTest {

    private lateinit var apiService: KthApiService
    private lateinit var dao: KthDao
    private lateinit var repository: KthRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        dao = mockk()
        repository = KthRepositoryImpl(apiService, dao)
    }

    @Test
    fun `getKthList should return data from DAO`() = runTest {
        val query = "Mekar"
        val dummyEntity = KthEntity("1", "KTH Mekar", "Desa ABC", "Kecamatan DEF","Kab XYZ", "Saya", "088829903982","KPH C")
        val expectedDomain = Kth("1", "KTH Mekar", "Desa ABC", "Kecamatan DEF","Kab XYZ", "Saya", "088829903982","KPH C")

        coEvery { dao.getAllKth(query) } returns flowOf(listOf(dummyEntity))

        val result = repository.getKthList(query).first()
        assertTrue(result is Resource.Success)
        assertEquals(1, result.data?.size)
        assertEquals(expectedDomain.name, result.data?.first()?.name)

        coVerify { dao.getAllKth(query) }
    }

    @Test
    fun `syncKthData should fetch from API and upsert to DAO`() = runTest {
        val dummyDto = KthListItemDto("1", "KTH Mekar", "Desa ABC", "Kab XYZ","KPH C")
        val apiResponse = ApiResponse(
            statusCode = 200,
            message = "Success",
            data = PaginatedData(
                data = listOf(dummyDto),
                count = 1,
                totalPages = 1,
            )
        )

        coEvery { apiService.getKthList(limit = 50) } returns apiResponse
        coEvery { dao.upsertAll(any()) } returns Unit

        val result = repository.syncKthData()
        assertTrue(result is Resource.Success)
        coVerify { apiService.getKthList(limit = 50) }
        coVerify { dao.upsertAll(any()) }
    }

    @Test
    fun `deleteKth should call API and delete from DAO`() = runTest {
        val id = "1"
        val apiResponse = ApiResponse<Unit>(200, "Deleted", Unit)

        coEvery { apiService.deleteKth(id) } returns apiResponse
        coEvery { dao.deleteKth(id) } returns Unit

        val result = repository.deleteKth(id)
        assertTrue(result is Resource.Success)
        coVerify { apiService.deleteKth(id) }
        coVerify { dao.deleteKth(id) }
    }
}