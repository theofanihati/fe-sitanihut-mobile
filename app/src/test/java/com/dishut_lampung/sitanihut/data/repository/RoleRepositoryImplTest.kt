package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.dao.RoleDao
import com.dishut_lampung.sitanihut.data.local.entity.RoleEntity
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.remote.api.RoleApiService
import com.dishut_lampung.sitanihut.data.remote.dto.RoleDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.domain.repository.RoleRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoleRepositoryImplTest {
    private lateinit var repository: RoleRepository
    private val apiService: RoleApiService = mockk()
    private val dao: RoleDao = mockk(relaxed = true)

    @Before
    fun setup() {
        repository = RoleRepositoryImpl(apiService, dao)
    }

    @Test
    fun `getRoles emits list of role from DAO`() = runTest {
        val entity = RoleEntity(id = "1", name = "petani")
        val expectedDomain = entity.toDomain()

        every { dao.getAllRoles() } returns flowOf(listOf(entity))

        val result = repository.getRoles().drop(1).first()
        assertTrue(result is Resource.Success)

        val data = result.data
        assertNotNull(data)
        assertEquals(1, data?.size)
        assertEquals(expectedDomain.name, data?.first()?.name)
        verify { dao.getAllRoles() }
    }

    @Test
    fun `syncRoleData returns Success and inserts ALL pages when API has multiple pages`() = runTest {
        val itemPage1 = RoleDto("1", "petani")
        val itemPage2 = RoleDto("2", "penyuluh")

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

        coEvery { apiService.getRoles(page = 1) } returns responsePage1
        coEvery { apiService.getRoles(page = 2) } returns responsePage2

        val result = repository.syncRoleData()
        assertTrue(result is Resource.Success)

        coVerify {
            dao.updateData(match { list ->
                list.size == 2 &&
                        list.any { it.id == "1" } &&
                        list.any { it.id == "2" }
            })
        }

        coVerify(exactly = 1) { apiService.getRoles(page = 1) }
        coVerify(exactly = 1) { apiService.getRoles(page = 2) }
    }

    @Test
    fun `syncRoleData handles single page correctly`() = runTest {
        val itemPage1 = RoleDto("1", "petani")
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 1, count = 1)
        val response = ApiResponse(200, "Success", page1Data)

        coEvery { apiService.getRoles(page = 1) } returns response

        val result = repository.syncRoleData()

        assertTrue(result is Resource.Success)
        coVerify { dao.updateData(match { it.size == 1 }) }
        coVerify(exactly = 0) { apiService.getRoles(page = 2) }
    }

    @Test
    fun `syncRoleData returns Error when Page 1 returns non-200`() = runTest {
        val errorResponse = ApiResponse<PaginatedData<RoleDto>>(
            statusCode = 400,
            message = "Bad Request",
            data = PaginatedData(emptyList(), 0, 0)
        )

        coEvery { apiService.getRoles(page = 1) } returns errorResponse
        val result = repository.syncRoleData()
        assertTrue(result is Resource.Error)
        assertEquals("Bad Request", result.message)

        coVerify(exactly = 0) { dao.deleteAll() }
    }

    @Test
    fun `syncRoleData returns Error when API throws exception on Page 1`() = runTest {
        val errorMessage = "Network Error"
        coEvery { apiService.getRoles(page = 1) } throws RuntimeException(errorMessage)

        val result = repository.syncRoleData()
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `syncRoleData continues and saves Page 1 data even if Page 2 fails`() = runTest {
        val itemPage1 = RoleDto("1", "petani")
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 2, count = 2)

        coEvery { apiService.getRoles(page = 1) } returns ApiResponse(200, "OK", page1Data)
        coEvery { apiService.getRoles(page = 2) } throws RuntimeException("Timeout")

        val result = repository.syncRoleData()
        assertTrue(result is Resource.Success)

        coVerify { dao.updateData(match { it.size == 1 && it[0].id == "1" }) }
    }
}