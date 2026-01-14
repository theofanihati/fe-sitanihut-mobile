package com.dishut_lampung.sitanihut.data.repository

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.dao.PetaniDao
import com.dishut_lampung.sitanihut.data.local.entity.PetaniEntity
import com.dishut_lampung.sitanihut.data.remote.api.PetaniApiService
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

        coEvery { apiService.getPetaniList(limit = 50) } returns apiResponse
        coEvery { dao.upsertAll(any()) } returns Unit

        val result = repository.syncPetaniData()
        assertTrue(result is Resource.Success)
        coVerify { apiService.getPetaniList(limit = 50) }
        coVerify { dao.upsertAll(any()) }
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
        val input = CreatePetaniInput(
            name = "Petani Update",
            identityNumber = "1234567890123456",
            gender = "Laki-laki",
            address = "Jl. Raya",
            whatsAppNumber = "08199",
            lastEducation = "SMA",
            sideJob = "Merajut",
            landArea = "100",
            kthId = "1",
        )

        val apiResponse = ApiResponse<Unit>(
            statusCode = 200,
            message = "Success Update",
            data = Unit
        )

        coEvery { apiService.updatePetani(eq(id), any()) } returns apiResponse
        val result = repository.updatePetani(id, input)
        assertTrue(result is Resource.Success)

        coVerify {
            apiService.updatePetani(eq(id), match {
                it.name == "Petani Update" && it.whatsAppNumber == "08199"
            })
        }
    }

    @Test
    fun `updateKth should return Error when API returns non-200 status`() = runTest {
        val id = "123"
        val input = CreatePetaniInput(
            name = "KTH Update",
            identityNumber = "", gender = "", address = "",
            whatsAppNumber = "", lastEducation = "", sideJob = "",
            landArea = "0",
        )
        val apiResponse = ApiResponse<Unit>(
            statusCode = 400,
            message = "Validasi Gagal",
            data = Unit
        )

        coEvery { apiService.updatePetani(id, any()) } returns apiResponse
        val result = repository.updatePetani(id, input)
        assertTrue(result is Resource.Error)
        assertEquals("Validasi Gagal", (result as Resource.Error).errorMessage)

        coVerify { apiService.updatePetani(id, any()) }
    }

    @Test
    fun `updateKth should return Error when Exception occurs`() = runTest {
        val id = "123"
        val input = CreatePetaniInput(
            name = "Petani Error", identityNumber = "",
            gender = "",
            address = "",
            whatsAppNumber = "",
            lastEducation = "",
            sideJob = "",
            landArea = "0",
            kthId = "0",
        )
        coEvery { apiService.updatePetani(id, any()) } throws RuntimeException("No Internet Connection")
        val result = repository.updatePetani(id, input)
        assertTrue(result is Resource.Error)
        assertEquals("No Internet Connection", (result as Resource.Error).errorMessage)
    }
}