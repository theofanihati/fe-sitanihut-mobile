package com.dishut_lampung.sitanihut.data.repository

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.dao.KthDao
import com.dishut_lampung.sitanihut.data.local.entity.KthEntity
import com.dishut_lampung.sitanihut.data.remote.api.KthApiService
import com.dishut_lampung.sitanihut.data.remote.dto.KthDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.KthListItemDto
import com.dishut_lampung.sitanihut.data.remote.dto.PenyuluhListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KthRepositoryImplTest {

    private lateinit var apiService: KthApiService
    private lateinit var dao: KthDao
    private lateinit var repository: KthRepositoryImpl
    val itemPage1 = KthListItemDto("1", "KTH Mekar", "Desa ABC", "Kab XYZ","123","KPH C")
    val itemPage2 = KthListItemDto("2", "KTH Kuncup", "Desa ABC", "Kab XYZ","123","KPH C")

    @Before
    fun setUp() {
        apiService = mockk()
        dao = mockk(relaxed = true)
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
        val apiResponse = ApiResponse(
            statusCode = 200,
            message = "Success",
            data = PaginatedData(
                data = listOf(itemPage1),
                count = 1,
                totalPages = 1,
            )
        )

        coEvery { apiService.getKthList(search = "", page = 1, limit = 50) } returns apiResponse
        coEvery { dao.updateData(any()) } returns Unit

        val result = repository.syncKthData()
        assertTrue(result is Resource.Success)
        coVerify { apiService.getKthList(search = "", page = 1, limit = 50) }
        coVerify { dao.updateData(any()) }
    }

    @Test
    fun `syncKthData when api returns empty should not insert`() = runTest {
        val paginatedData = PaginatedData(
            data = emptyList<KthListItemDto>(),
            totalPages = 0,
            count = 0
        )
        val apiResponse = ApiResponse(200, "success", paginatedData)
        coEvery { apiService.getKthList(search = "", page = 1, limit = 50) } returns apiResponse

        val result = repository.syncKthData()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 0) { dao.updateData(any()) }
    }

    @Test
    fun `syncKthData success should verify ALL fields mapping correctly`() = runTest {
        val paginatedData = PaginatedData(
            data = listOf(itemPage1),
            totalPages = 1,
            count = 1
        )
        val apiResponse = ApiResponse(200, "ok", paginatedData)
        coEvery { apiService.getKthList(search = "", page = 1, limit = 50) } returns apiResponse

        val result = repository.syncKthData()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { dao.updateData(
            match { list ->
                val entity = list.first()
                entity.id == "1" &&
                        entity.name == "KTH Mekar" &&
                        entity.desa == "Desa ABC" &&
                        entity.kabupaten == "Kab XYZ" &&
                        entity.kphId == "123" &&
                        entity.kphName == "KPH C"
            })
        }
    }

    @Test
    fun `syncKthData returns Success and inserts ALL pages when API has multiple pages`() = runTest {
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

        coEvery { apiService.getKthList(search = "", page = 1, limit = 50) } returns responsePage1
        coEvery { apiService.getKthList(search = "", page = 2, limit = 50) } returns responsePage2

        val result = repository.syncKthData()
        assertTrue(result is Resource.Success)

        coVerify {
            dao.updateData(match { list ->
                list.size == 2 &&
                        list.any { it.id == "1" } &&
                        list.any { it.id == "2" }
            })
        }

        coVerify(exactly = 1) { apiService.getKthList(search = "", page = 1, limit = 50) }
        coVerify(exactly = 1) { apiService.getKthList(search = "", page = 2, limit = 50) }
    }

    @Test
    fun `syncKthData handles single page correctly`() = runTest {
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 1, count = 1)
        val response = ApiResponse(200, "Success", page1Data)

        coEvery { apiService.getKthList(search = "", page = 1, limit = 50) } returns response

        val result = repository.syncKthData()

        assertTrue(result is Resource.Success)
        coVerify { dao.updateData(match { it.size == 1 }) }
        coVerify(exactly = 0) { apiService.getKthList(search = "", page = 2, limit = 50) }
    }

    @Test
    fun `syncKthData returns Error when Page 1 returns non-200`() = runTest {
        val errorResponse = ApiResponse<PaginatedData<KthListItemDto>>(
            statusCode = 400,
            message = "Bad Request",
            data = PaginatedData(emptyList(), 0, 0)
        )

        coEvery { apiService.getKthList(search = "", page = 1, limit = 50) } returns errorResponse
        val result = repository.syncKthData()
        assertTrue(result is Resource.Error)
        assertEquals("Bad Request", result.message)

        coVerify(exactly = 0) { dao.deleteAll() }
    }

    @Test
    fun `syncKthData returns Error when API throws exception on Page 1`() = runTest {
        val errorMessage = "Network Error"
        coEvery { apiService.getKthList(search = "", page = 1, limit = 50) } throws RuntimeException(errorMessage)

        val result = repository.syncKthData()
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `syncKthData continues and saves Page 1 data even if Page 2 fails`() = runTest {
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 2, count = 2)

        coEvery { apiService.getKthList(search = "", page = 1, limit = 50) } returns ApiResponse(200, "OK", page1Data)
        coEvery { apiService.getKthList(search = "", page = 2, limit = 50) } throws RuntimeException("Timeout")

        val result = repository.syncKthData()
        assertTrue(result is Resource.Success)

        coVerify { dao.updateData(match { it.size == 1 && it[0].id == "1" }) }
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

    @Test
    fun `getKthDetail should return mapped domain model from DAO`() = runTest {
        val id = "123"
        val dummyEntity = KthEntity(
            id = id,
            name = "KTH Test",
            desa = "Desa Test",
            kecamatan = "Kec Test",
            kabupaten = "Kab Test",
            coordinator = "Ketua Test",
            whatsappNumber = "08123456789",
            kphName = "KPH Test"
        )

        every { dao.getKthById(id) } returns flowOf(dummyEntity)

        repository.getKthDetail(id).test {
            val firstItem = awaitItem()
            assertTrue(firstItem is Resource.Loading)

            val secondItem = awaitItem()
            assertTrue(secondItem is Resource.Success)

            val data = (secondItem as Resource.Success).data
            assertEquals("KTH Test", data?.name)

            cancelAndIgnoreRemainingEvents()
        }
        coVerify { dao.getKthById(id) }
    }

    @Test
    fun `getKthDetail should emit Error if data not found`() = runTest {
        val id = "999"
        val dummyEmptyData = KthDetailDto(
            id = "", name = "", desa = "", kecamatan = "",
            kabupaten = "", coordinator = "", whatsappNumber = "", kphId = "", kphName = ""
        )
        every { dao.getKthById(id) } returns flowOf(null)

        val errorResponse = ApiResponse<KthDetailDto>(404, "Not Found", dummyEmptyData)
        coEvery { apiService.getKthDetail(id) } returns errorResponse

        val result = repository.getKthDetail(id).toList()
        assertTrue(result.first() is Resource.Loading)
        assertTrue(result.last() is Resource.Error)
        assertEquals("Not Found", (result.last() as Resource.Error).errorMessage)
        coVerify { dao.getKthById(id) }
    }

    @Test
    fun `createKth should call api service with correct body`() = runTest {
        val input = CreateKthInput(
            name = "KTH Baru",
            desa = "Desa A",
            kecamatan = "Kec B",
            kabupaten = "Kab C",
            coordinator = "Budi",
            whatsappNumber = "08123",
            kphId = "1",
            kphName = "KPH X"
        )

        val dummyResponseData = KthDetailDto(
            id = "new-id",
            name = "KTH Baru",
            desa = "Desa A",
            kecamatan = "Kec B",
            kabupaten = "Kab C",
            coordinator = "Budi",
            whatsappNumber = "08123",
            kphId = "1",
            kphName = "KPH X"
        )
        val apiResponse = ApiResponse(
            statusCode = 201,
            message = "Success",
            data = dummyResponseData
        )

        coEvery { apiService.createKth(any()) } returns apiResponse
        coEvery { dao.upsertAll(any()) } returns Unit

        val result = repository.createKth(input)

        assertTrue(result is Resource.Success)

        coVerify {
            apiService.createKth(match {
                it.name == "KTH Baru" && it.kphId == "1"
            })
        }
    }

    @Test
    fun `updateKth should call api service and return Success when status is 200`() = runTest {
        val id = "123"
        val input = CreateKthInput(
            name = "KTH Update",
            desa = "Desa Update",
            kecamatan = "Kec Update",
            kabupaten = "Kab U",
            coordinator = "Coord Update",
            whatsappNumber = "08199",
            kphId = "2",
            kphName = "KPH Y"
        )

        val apiResponse = ApiResponse<Unit>(
            statusCode = 200,
            message = "Success Update",
            data = Unit
        )

        coEvery { apiService.updateKth(eq(id), any()) } returns apiResponse
        val result = repository.updateKth(id, input)
        assertTrue(result is Resource.Success)

        coVerify {
            apiService.updateKth(eq(id), match {
                it.name == "KTH Update" && it.whatsappNumber == "08199"
            })
        }
    }

    @Test
    fun `updateKth should return Error when API returns non-200 status`() = runTest {
        val id = "123"
        val input = CreateKthInput(
            name = "KTH Update", desa = "Update", kecamatan = "Update", kabupaten = "Update",
            coordinator = "Update", whatsappNumber = "08", kphId = "1", kphName = "Update"
        )
        val apiResponse = ApiResponse<Unit>(
            statusCode = 400,
            message = "Validasi Gagal",
            data = Unit
        )

        coEvery { apiService.updateKth(id, any()) } returns apiResponse
        val result = repository.updateKth(id, input)
        assertTrue(result is Resource.Error)
        assertEquals("Validasi Gagal", (result as Resource.Error).errorMessage)

        coVerify { apiService.updateKth(id, any()) }
    }

    @Test
    fun `updateKth should return Error when Exception occurs`() = runTest {
        val id = "123"
        val input = CreateKthInput(
            name = "KTH Error", desa = "", kecamatan = "", kabupaten = "",
            coordinator = "", whatsappNumber = "", kphId = "", kphName = ""
        )
        coEvery { apiService.updateKth(id, any()) } throws RuntimeException("No Internet Connection")
        val result = repository.updateKth(id, input)
        assertTrue(result is Resource.Error)
        assertEquals("No Internet Connection", (result as Resource.Error).errorMessage)
    }
}