package com.dishut_lampung.sitanihut.data.repository

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.dao.PetaniDao
import com.dishut_lampung.sitanihut.data.local.entity.PetaniEntity
import com.dishut_lampung.sitanihut.data.remote.api.PetaniApiService
import com.dishut_lampung.sitanihut.data.remote.dto.PenyuluhListItemDto
import com.dishut_lampung.sitanihut.data.remote.dto.PetaniDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.PetaniListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PetaniRepositoryImplTest {

    private lateinit var apiService: PetaniApiService
    private lateinit var dao: PetaniDao
    private lateinit var repository: PetaniRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        dao = mockk()
        repository = PetaniRepositoryImpl(apiService, dao)
        coEvery { dao.updateData(any()) } returns Unit
    }

    @Test
    fun `getPetaniList should return data from DAO`() = runTest {
        val query = "Mekar"
        val dummyEntity = PetaniEntity(
            id = "1",
            name = "Paneeyy",
            identityNumber = "1234567890123456",
            gender = "Cewe lah",
            address = "Jl. Raya",
            whatsAppNumber = "081234567890",
            lastEducation = "SMA",
            sideJob = "Merajut",
            landArea = 100.0,
            kphId = "123",
            kphName = "KPH C",
            kthId = "123",
            kthName = "KTH C"
        )
        val expectedDomain = Petani(
            id = "1",
            name = "Paneeyy",
            identityNumber = "1234567890123456",
            gender = "Cewe lah",
            address = "Jl. Raya",
            whatsAppNumber = "081234567890",
            lastEducation = "SMA",
            sideJob = "Merajut",
            landArea = 100.0,
            kphName = "KPH C",
            kthName = "KTH C"
        )

        coEvery { dao.getAllPetani(query) } returns flowOf(listOf(dummyEntity))

        val result = repository.getPetaniList(query).first()
        assertTrue(result is Resource.Success)
        assertEquals(1, result.data?.size)
        assertEquals(expectedDomain.name, result.data?.first()?.name)

        coVerify { dao.getAllPetani(query) }
    }

    @Test
    fun `syncPetaniData should fetch from API and upsert to DAO`() = runTest {
        val dummyDto = PetaniListItemDto(
            id = "1",
            name = "Paneeyy",
            identityNumber = "1234567890123456",
            kphId = "1",
            kphName = "KPH C",
            kthId = "1",
            kthName = "KTH C",
        )
        val apiResponse = ApiResponse(
            statusCode = 200,
            message = "Success",
            data = PaginatedData(
                data = listOf(dummyDto),
                count = 1,
                totalPages = 1,
            )
        )

        coEvery { apiService.getPetaniList(search = "", page = 1, limit = 50) } returns apiResponse
        coEvery { dao.updateData(any()) } returns Unit

        val result = repository.syncPetaniData()
        assertTrue(result is Resource.Success)
        coVerify { apiService.getPetaniList(search = "", page = 1, limit = 50) }
        coVerify { dao.updateData(any()) }
    }

    @Test
    fun `syncPetaniData when api returns empty should not insert`() = runTest {
        val paginatedData = PaginatedData(
            data = emptyList<PetaniListItemDto>(),
            totalPages = 0,
            count = 0
        )
        val apiResponse = ApiResponse(200, "success", paginatedData)
        coEvery { apiService.getPetaniList(search = "", page = 1, limit = 50) } returns apiResponse

        val result = repository.syncPetaniData()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 0) { dao.updateData(any()) }
    }

    @Test
    fun `syncPetaniData success should verify ALL fields mapping correctly`() = runTest {
        val dto = PetaniListItemDto(
            id = "1",
            name = "Paneeyy",
            identityNumber = "1234567890123456",
            kphId = "1",
            kphName = "KPH C",
            kthId = "1",
            kthName = "KTH C",
        )
        val paginatedData = PaginatedData(
            data = listOf(dto),
            totalPages = 1,
            count = 1
        )
        val apiResponse = ApiResponse(200, "ok", paginatedData)
        coEvery { apiService.getPetaniList(search = "", page = 1, limit = 50) } returns apiResponse

        val result = repository.syncPetaniData()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { dao.updateData(
            match { list ->
                val entity = list.first()
                entity.id == "1" &&
                        entity.name == "Paneeyy" &&
                        entity.identityNumber == "1234567890123456" &&
                        entity.kphId == "1" &&
                        entity.kphName == "KPH C" &&
                        entity.kthId == "1" &&
                        entity.kthName == "KTH C"
            })
        }
    }

    @Test
    fun `syncPetaniData returns Success and inserts ALL pages when API has multiple pages`() = runTest {
        val itemPage1 = PetaniListItemDto(
            id = "1",
            name = "Paneeyy",
            identityNumber = "1234567890123456",
            kphId = "1",
            kphName = "KPH C",
            kthId = "1",
            kthName = "KTH C",
        )
        val itemPage2 = PetaniListItemDto(
            id = "2",
            name = "Tepaaannnn",
            identityNumber = "1234567890123457",
            kphId = "1",
            kphName = "KPH C",
            kthId = "1",
            kthName = "KTH C",
        )

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

        coEvery { apiService.getPetaniList(search = "", page = 1, limit = 50) } returns responsePage1
        coEvery { apiService.getPetaniList(search = "", page = 2, limit = 50) } returns responsePage2

        val result = repository.syncPetaniData()
        assertTrue(result is Resource.Success)

        coVerify {
            dao.updateData(match { list ->
                list.size == 2 &&
                        list.any { it.id == "1" } &&
                        list.any { it.id == "2" }
            })
        }

        coVerify(exactly = 1) { apiService.getPetaniList(search = "", page = 1, limit = 50) }
        coVerify(exactly = 1) { apiService.getPetaniList(search = "", page = 2, limit = 50) }
    }

    @Test
    fun `syncPetaniData handles single page correctly`() = runTest {
        val itemPage1 = PetaniListItemDto(
            id = "1",
            name = "Paneeyy",
            identityNumber = "1234567890123456",
            kphId = "1",
            kphName = "KPH C",
            kthId = "1",
            kthName = "KTH C",
        )
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 1, count = 1)
        val response = ApiResponse(200, "Success", page1Data)

        coEvery { apiService.getPetaniList(search = "", page = 1, limit = 50) } returns response

        val result = repository.syncPetaniData()

        assertTrue(result is Resource.Success)
        coVerify { dao.updateData(match { it.size == 1 }) }
        coVerify(exactly = 0) { apiService.getPetaniList(search = "", page = 2, limit = 50) }
    }

    @Test
    fun `syncPetaniData returns Error when Page 1 returns non-200`() = runTest {
        val errorResponse = ApiResponse<PaginatedData<PetaniListItemDto>>(
            statusCode = 400,
            message = "Bad Request",
            data = PaginatedData(emptyList(), 0, 0)
        )

        coEvery { apiService.getPetaniList(search = "", page = 1, limit = 50) } returns errorResponse
        val result = repository.syncPetaniData()
        assertTrue(result is Resource.Error)
        assertEquals("Bad Request", result.message)

        coVerify(exactly = 0) { dao.deleteAll() }
    }

    @Test
    fun `syncPetaniData returns Error when API throws exception on Page 1`() = runTest {
        val errorMessage = "Network Error"
        coEvery { apiService.getPetaniList(search = "", page = 1, limit = 50) } throws RuntimeException(errorMessage)

        val result = repository.syncPetaniData()
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `syncPetaniData continues and saves Page 1 data even if Page 2 fails`() = runTest {
        val itemPage1 = PetaniListItemDto(
            id = "1",
            name = "Paneeyy",
            identityNumber = "1234567890123456",
            kphId = "1",
            kphName = "KPH C",
            kthId = "1",
            kthName = "KTH C",
        )
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 2, count = 2)

        coEvery { apiService.getPetaniList(search = "", page = 1, limit = 50) } returns ApiResponse(200, "OK", page1Data)
        coEvery { apiService.getPetaniList(search = "", page = 2, limit = 50) } throws RuntimeException("Timeout")

        val result = repository.syncPetaniData()
        assertTrue(result is Resource.Success)

        coVerify { dao.updateData(match { it.size == 1 && it[0].id == "1" }) }
    }

    @Test
    fun `deletePetani should call API and delete from DAO`() = runTest {
        val id = "1"
        val apiResponse = ApiResponse<Unit>(200, "Deleted", Unit)

        coEvery { apiService.deletePetani(id) } returns apiResponse
        coEvery { dao.deletePetani(id) } returns Unit

        val result = repository.deletePetani(id)
        assertTrue(result is Resource.Success)
        coVerify { apiService.deletePetani(id) }
        coVerify { dao.deletePetani(id) }
    }

    @Test
    fun `getPetaniDetail should return mapped domain model from DAO`() = runTest {
        val id = "123"
        val dummyEntity = PetaniEntity(
            id = "1",
            name = "Paneeyy",
            identityNumber = "1234567890123456",
            gender = "Cewe lah",
            address = "Jl. Raya",
            whatsAppNumber = "081234567890",
            lastEducation = "SMA",
            sideJob = "Merajut",
            landArea = 100.0,
            kphId = "123",
            kphName = "KPH C",
            kthId = "123",
            kthName = "KTH C",
        )

        every { dao.getPetaniById(id) } returns flowOf(dummyEntity)
        coEvery { apiService.getPetaniDetail(id) } throws Exception("Network skipped for local test")

        repository.getPetaniDetail(id).test {
            val firstItem = awaitItem()
            assertTrue(firstItem is Resource.Loading)

            val secondItem = awaitItem()
            assertTrue(secondItem is Resource.Success)

            val data = (secondItem as Resource.Success).data
            assertEquals("Paneeyy", data?.name)

            cancelAndIgnoreRemainingEvents()
        }
        coVerify { dao.getPetaniById(id) }
    }

    @Test
    fun `getPetaniDetail should emit Error if data not found`() = runTest {
        val id = "999"
        val dummyEmptyData = PetaniDetailDto(
            id = "", name = "",
            identityNumber = "", gender = "", address = "",
            whatsAppNumber = "", lastEducation = "", sideJob = "",
            landArea = 0.0, kphId = "", kphName = "", kthId = "", kthName = ""
        )
        every { dao.getPetaniById(id) } returns flowOf(null)

        val errorResponse = ApiResponse<PetaniDetailDto>(404, "Not Found", dummyEmptyData)
        coEvery { apiService.getPetaniDetail(id) } returns errorResponse

        val result = repository.getPetaniDetail(id).toList()
        assertTrue(result.first() is Resource.Loading)
        assertTrue(result.last() is Resource.Error)
        assertEquals("Not Found", (result.last() as Resource.Error).errorMessage)
        coVerify { dao.getPetaniById(id) }
    }

    @Test
    fun `createPetani should call api service with correct body`() = runTest {
        val input = CreatePetaniInput(
            name = "Petani Baru",
            identityNumber = "1234567890123456",
            gender = "Laki-laki",
            address = "Jl. Raya",
            whatsAppNumber = "081234567890",
            lastEducation = "SMA",
            sideJob = "Merajut",
            landArea = "100",
            kthId = "1",
        )

        val dummyResponseData = PetaniDetailDto(
            id = "new-id",
            name = "Petani Baru",
            identityNumber = "1234567890123456",
            gender = "Laki-laki",
            address = "Jl. Raya",
            whatsAppNumber = "081234567890",
            lastEducation = "SMA",
            sideJob = "Merajut",
            landArea = 100.0,
            kthId = "1",
            kthName = "KTH C",
            kphId = "1",
            kphName = "KPH C",
        )
        val apiResponse = ApiResponse(
            statusCode = 201,
            message = "Success",
            data = dummyResponseData
        )

        coEvery { apiService.createPetani(any()) } returns apiResponse
        coEvery { dao.upsertAll(any()) } returns Unit

        val result = repository.createPetani(input)

        assertTrue(result is Resource.Success)

        coVerify {
            apiService.createPetani(match {
                it.name == "Petani Baru" && it.kthId == "1"
            })
        }
    }

    @Test
    fun `updateKth should call api service and return Success when status is 200`() = runTest {
        val id = "123"
        val changes = mapOf(
            "nama_petani" to "Petani Update",
            "nomor_wa" to "08199"
        )

        val apiResponse = ApiResponse<Unit>(
            statusCode = 200,
            message = "Success Update",
            data = Unit
        )

        coEvery { apiService.updatePetani(eq(id), any()) } returns apiResponse
        val result = repository.updatePetani(id, changes)
        assertTrue(result is Resource.Success)

        coVerify {
            apiService.updatePetani(eq(id), match { map ->
                map["nama_petani"] == "Petani Update" && map["nomor_wa"] == "08199"
            })
        }
    }

    @Test
    fun `updateKth should return Error when API returns non-200 status`() = runTest {
        val id = "123"
        val changes = mapOf(
            "nama_petani" to "KTH Update"
        )
        val apiResponse = ApiResponse<Unit>(
            statusCode = 400,
            message = "Validasi Gagal",
            data = Unit
        )

        coEvery { apiService.updatePetani(id, any()) } throws RuntimeException("Validasi Gagal")
        val result = repository.updatePetani(id, changes)
        assertTrue(result is Resource.Error)
        assertEquals("Validasi Gagal", (result as Resource.Error).errorMessage)

        coVerify { apiService.updatePetani(id, any()) }
    }

    @Test
    fun `updateKth should return Error when Exception occurs`() = runTest {
        val id = "123"
        val changes = mapOf<String, Any?>()
        coEvery { apiService.updatePetani(id, any()) } throws RuntimeException("No Internet Connection")
        val result = repository.updatePetani(id, changes)
        assertTrue(result is Resource.Error)
        assertEquals("No Internet Connection", (result as Resource.Error).errorMessage)
    }
}