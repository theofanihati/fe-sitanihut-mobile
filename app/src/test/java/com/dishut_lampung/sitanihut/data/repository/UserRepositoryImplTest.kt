package com.dishut_lampung.sitanihut.data.repository

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.RoleDao
import com.dishut_lampung.sitanihut.data.local.dao.UserDao
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity
import com.dishut_lampung.sitanihut.data.mapper.toDomain
import com.dishut_lampung.sitanihut.data.remote.api.UserApiService
import com.dishut_lampung.sitanihut.data.remote.dto.PetaniDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.RoleDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class UserRepositoryImplTest {

    private var apiService: UserApiService = mockk()
    private var userDao: UserDao = mockk()
    private var roleDao: RoleDao = mockk(relaxed = true)
    private val preferences: UserPreferences = mockk(relaxed = true)
    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setUp() {
        repository = UserRepositoryImpl(apiService, userDao, roleDao, preferences )
    }

    @Test
    fun `getUserList should return data from DAO`() = runTest {
        val query = "petani"
        val dummyEntity = UserEntity(
            id = "1",
            role = "petani",
            name = "Paneeyy",
            gender = "Cewe lah",
        )
        val expectedDomain = UserDetail(
            id = "1",
            role = "petani",
            name = "Paneeyy",
            gender = "Cewe lah",
        )

        coEvery { userDao.getAllUser(query) } returns flowOf(listOf(dummyEntity))

        val result = repository.getUserList(query).first()
        assertTrue(result is Resource.Success)
        assertEquals(1, result.data?.size)
        assertEquals(expectedDomain.name, result.data?.first()?.name)

        coVerify { userDao.getAllUser(query) }
    }

    @Test
    fun `syncUserData should fetch from API and upsert to DAO`() = runTest {
        val dummyDto = UserListItemDto(
            id = "1",
            name = "Paneeyy",
            role = "petani",
            gender = "Cewe lah",
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

        coEvery { apiService.getUserList(limit = 50) } returns apiResponse
        coEvery { userDao.upsertAll(any()) } returns Unit

        val result = repository.syncUserData()
        assertTrue(result is Resource.Success)
        coVerify { apiService.getUserList(limit = 50) }
        coVerify { userDao.upsertAll(any()) }
    }

    @Test
    fun `deleteUser should call API and delete from DAO`() = runTest {
        val id = "1"
        val apiResponse = ApiResponse<Unit>(200, "Deleted", Unit)

        coEvery { apiService.deleteUser(id) } returns apiResponse
        coEvery { userDao.deleteUser(id) } returns Unit

        val result = repository.deleteUser(id)
        assertTrue(result is Resource.Success)
        coVerify { apiService.deleteUser(id) }
        coVerify { userDao.deleteUser(id) }
    }

    @Test
    fun `getUserDetail should return mapped domain model from DAO`() = runTest {
        val id = "123"
        val dummyEntity = UserEntity(
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

        every { userDao.getUserById(id) } returns flowOf(dummyEntity)
        coEvery { apiService.getUserDetail(id) } throws Exception("Network skipped for local test")

        repository.getUserDetail(id).test {
            val firstItem = awaitItem()
            assertTrue(firstItem is Resource.Loading)

            val secondItem = awaitItem()
            assertTrue(secondItem is Resource.Success)

            val data = (secondItem as Resource.Success).data
            assertEquals("Paneeyy", data?.name)

            cancelAndIgnoreRemainingEvents()
        }
        coVerify { userDao.getUserById(id) }
    }

    @Test
    fun `getUserDetail should emit Error if data not found`() = runTest {
        val id = "999"
        val dummyEmptyData = UserDetailDto(
            id = "", name = "", email = "",roleId = "",
            identityNumber = "", gender = "", address = "",
            whatsAppNumber = "", lastEducation = "", sideJob = "",
            landArea = 0.0, kphId = "", kphName = "", kthId = "", kthName = ""
        )
        every { userDao.getUserById(id) } returns flowOf(null)

        val errorResponse = ApiResponse<UserDetailDto>(404, "Not Found", dummyEmptyData)
        coEvery { apiService.getUserDetail(id) } returns errorResponse

        val result = repository.getUserDetail(id).toList()
        assertTrue(result.first() is Resource.Loading)
        assertTrue(result.last() is Resource.Error)
        assertEquals("Not Found", (result.last() as Resource.Error).errorMessage)
        coVerify { userDao.getUserById(id) }
    }

    @Test
    fun `getUserDetail should fetch roles from API if role name not found in DAO`() = runTest {
        val userId = "user123"
        val roleId = "role_petani"
        val roleName = "Petani"

        every { userDao.getUserById(userId) } returns flowOf(null)

        val remoteUserDto = UserDetailDto(
            id = userId, roleId = roleId, name = "Budi",
            email = "", identityNumber = "", gender = "", address = "",
            whatsAppNumber = "", lastEducation = "", sideJob = "",
            landArea = 0.0, kphId = "", kphName = "", kthId = "", kthName = ""
        )
        val userApiResponse = ApiResponse(
            200,
            "OK",
            remoteUserDto)
        coEvery { apiService.getUserDetail(userId) } returns userApiResponse
        coEvery { roleDao.getRoleName(roleId) } returns null andThen roleName

        val rolesApiResponse = ApiResponse(
            200,
            "OK",
            listOf(RoleDto(id = roleId, name = roleName)))
        coEvery { apiService.getRoles() } returns rolesApiResponse
        coEvery { roleDao.insertRoles(any()) } returns Unit
        coEvery { userDao.upsertAll(any()) } returns Unit

        repository.getUserDetail(userId).test {
            awaitItem()
            val successItem = awaitItem()
            assertTrue(successItem is Resource.Success)
            assertEquals(roleName, successItem.data?.role)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { apiService.getRoles() }
        coVerify(exactly = 1) { roleDao.insertRoles(any()) }
        coVerify {
            userDao.upsertAll(match { users ->
                users.first().role == roleName
            })
        }
    }

    @Test
    fun `getUserDetail should fallback to UserPreferences for role name if resolve fails`() = runTest {
        val id = "123"
        val roleId = "unknown_role"

        every { userDao.getUserById(id) } returns flowOf(null)
        coEvery { apiService.getUserDetail(id) } returns ApiResponse(200, "OK",
            UserDetailDto(
                id=id, roleId=roleId, name="User", email="",
                identityNumber="", gender="", address="", whatsAppNumber="",
                lastEducation="", sideJob="", landArea=0.0, kphId="", kphName="",
                kthId="", kthName=""
            )
        )

        coEvery { roleDao.getRoleName(roleId) } returns null
        coEvery { apiService.getRoles() } throws RuntimeException("API Error")

        every { preferences.userRole } returns flowOf("petani")

        coEvery { userDao.upsertAll(any()) } returns Unit

        repository.getUserDetail(id).test {
            awaitItem()
            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertEquals("Petani", result.data?.role)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createUser should call api service with correct body`() = runTest {
        val input = CreateUserInput(
            name = "Petani Baru",
            email = "petani@gmail.com",
            roleId = "1",
            identityNumber = "1234567890123456",
            gender = "Laki-laki",
            address = "Jl. Raya",
            whatsAppNumber = "081234567890",
            lastEducation = "SMA",
            sideJob = "Merajut",
            landArea = "100",
            kthId = "1",
        )

        val dummyResponseData = UserDetailDto(
            id = "new-id",
            name = "Petani Baru",
            email = "petani@gmail.com",
            roleId = "1",
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

        coEvery { apiService.createUser(any()) } returns apiResponse
        coEvery { userDao.upsertAll(any()) } returns Unit

        val result = repository.createUser(input)

        assertTrue(result is Resource.Success)

        coVerify {
            apiService.createUser(match {
                it.name == "Petani Baru" && it.kthId == "1"
            })
        }
    }

    @Test
    fun `updateUser should call api service and return Success when status is 200`() = runTest {
        val id = "123"
        val changes = mapOf(
            "nama_user" to "User Update",
            "nomor_wa" to "08199"
        )

        val apiResponse = ApiResponse<Unit>(
            statusCode = 200,
            message = "Success Update",
            data = Unit
        )

        coEvery { apiService.updateUser(eq(id), any()) } returns apiResponse
        val result = repository.updateUser(id, changes)
        assertTrue(result is Resource.Success)

        coVerify {
            apiService.updateUser(eq(id), match { map ->
                map["nama_user"] == "User Update" && map["nomor_wa"] == "08199"
            })
        }
    }

    @Test
    fun `updateUser should return Error when API returns non-200 status`() = runTest {
        val id = "123"
        val changes = mapOf(
            "nama_user" to "User Update"
        )
        val apiResponse = ApiResponse<Unit>(
            statusCode = 400,
            message = "Validasi Gagal",
            data = Unit
        )

        coEvery { apiService.updateUser(id, any()) } throws RuntimeException("Validasi Gagal")
        val result = repository.updateUser(id, changes)
        assertTrue(result is Resource.Error)
        assertEquals("Validasi Gagal", (result as Resource.Error).errorMessage)

        coVerify { apiService.updateUser(id, any()) }
    }

    @Test
    fun `updateUser should return Error when Exception occurs`() = runTest {
        val id = "123"
        val changes = mapOf<String, Any?>()
        coEvery { apiService.updateUser(id, any()) } throws RuntimeException("No Internet Connection")
        val result = repository.updateUser(id, changes)
        assertTrue(result is Resource.Error)
        assertEquals("No Internet Connection", (result as Resource.Error).errorMessage)
    }
}