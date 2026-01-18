package com.dishut_lampung.sitanihut.data.repository

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.RoleDao
import com.dishut_lampung.sitanihut.data.local.dao.UserDao
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity
import com.dishut_lampung.sitanihut.data.remote.api.UserApiService
import com.dishut_lampung.sitanihut.data.remote.dto.RoleDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.model.UserDetail
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

class UserRepositoryImplTest {
    private var apiService: UserApiService = mockk()
    private var userDao: UserDao = mockk(relaxed = true)
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
            kphId = "123",
            kphName = "KPH C",
            kthId = "123",
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

        coEvery { apiService.getUserList(search = "", page = 1, limit = 50) } returns apiResponse
        coEvery { userDao.updateData(any()) } returns Unit

        val result = repository.syncUserData()
        assertTrue(result is Resource.Success)
        coVerify { apiService.getUserList(search = "", page = 1, limit = 50) }
        coVerify { userDao.updateData(any()) }
    }

    @Test
    fun `syncUserData when api returns empty should not insert`() = runTest {
        val paginatedData = PaginatedData(
            data = emptyList<UserListItemDto>(),
            totalPages = 0,
            count = 0
        )
        val apiResponse = ApiResponse(200, "success", paginatedData)
        coEvery { apiService.getUserList(search = "", page = 1, limit = 50) } returns apiResponse

        val result = repository.syncUserData()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 0) { userDao.updateData(any()) }
    }

    @Test
    fun `syncUserData success should verify ALL fields mapping correctly`() = runTest {
        val dto = UserListItemDto(
            id = "1",
            name = "Paneeyy",
            role = "petani",
            gender = "Cewe lah",
            kphId = "123",
            kphName = "KPH C",
            kthId = "123",
            kthName = "KTH C",
        )
        val paginatedData = PaginatedData(
            data = listOf(dto),
            totalPages = 1,
            count = 1
        )
        val apiResponse = ApiResponse(200, "ok", paginatedData)
        coEvery { apiService.getUserList(search = "", page = 1, limit = 50) } returns apiResponse

        val result = repository.syncUserData()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { userDao.updateData(
            match { list ->
                val entity = list.first()
                entity.id == "1" &&
                        entity.name == "Paneeyy" &&
                        entity.role == "petani" &&
                        entity.gender == "Cewe lah" &&
                        entity.kphId == "123" &&
                        entity.kphName == "KPH C" &&
                        entity.kthId == "123" &&
                        entity.kthName == "KTH C"
            })
        }
    }

    @Test
    fun `syncUserData returns Success and inserts ALL pages when API has multiple pages`() = runTest {
        val itemPage1 = UserListItemDto(
            id = "1",
            name = "Paneeyy",
            role = "petani",
            gender = "Cewe lah",
            kphId = "123",
            kphName = "KPH C",
            kthId = "123",
            kthName = "KTH C",
        )
        val itemPage2 = UserListItemDto(
            id = "2",
            name = "Tepaaannn",
            role = "petani",
            gender = "Cowo lah",
            kphId = "123",
            kphName = "KPH C",
            kthId = "123",
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

        coEvery { apiService.getUserList(search = "", page = 1, limit = 50) } returns responsePage1
        coEvery { apiService.getUserList(search = "", page = 2, limit = 50) } returns responsePage2

        val result = repository.syncUserData()
        assertTrue(result is Resource.Success)

        coVerify {
            userDao.updateData(match { list ->
                list.size == 2 &&
                        list.any { it.id == "1" } &&
                        list.any { it.id == "2" }
            })
        }

        coVerify(exactly = 1) { apiService.getUserList(search = "", page = 1, limit = 50) }
        coVerify(exactly = 1) { apiService.getUserList(search = "", page = 2, limit = 50) }
    }

    @Test
    fun `syncUserData handles single page correctly`() = runTest {
        val itemPage1 = UserListItemDto(
            id = "1",
            name = "Paneeyy",
            role = "petani",
            gender = "Cewe lah",
            kphId = "123",
            kphName = "KPH C",
            kthId = "123",
            kthName = "KTH C",
        )
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 1, count = 1)
        val response = ApiResponse(200, "Success", page1Data)

        coEvery { apiService.getUserList(search = "", page = 1, limit = 50) } returns response

        val result = repository.syncUserData()

        assertTrue(result is Resource.Success)
        coVerify { userDao.updateData(match { it.size == 1 }) }
        coVerify(exactly = 0) { apiService.getUserList(search = "", page = 2, limit = 50) }
    }

    @Test
    fun `syncUserData returns Error when Page 1 returns non-200`() = runTest {
        val errorResponse = ApiResponse<PaginatedData<UserListItemDto>>(
            statusCode = 400,
            message = "Bad Request",
            data = PaginatedData(emptyList(), 0, 0)
        )

        coEvery { apiService.getUserList(search = "", page = 1, limit = 50) } returns errorResponse
        val result = repository.syncUserData()
        assertTrue(result is Resource.Error)
        assertEquals("Bad Request", result.message)

        coVerify(exactly = 0) { userDao.deleteAll() }
    }

    @Test
    fun `syncUserData returns Error when API throws exception on Page 1`() = runTest {
        val errorMessage = "Network Error"
        coEvery { apiService.getUserList(search = "", page = 1, limit = 50) } throws RuntimeException(errorMessage)

        val result = repository.syncUserData()
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `syncUserData continues and saves Page 1 data even if Page 2 fails`() = runTest {
        val itemPage1 = UserListItemDto(
            id = "1",
            name = "Paneeyy",
            role = "petani",
            gender = "Cewe lah",
            kphId = "123",
            kphName = "KPH C",
            kthId = "123",
            kthName = "KTH C",
        )
        val page1Data = PaginatedData(listOf(itemPage1), totalPages = 2, count = 2)

        coEvery { apiService.getUserList(search = "", page = 1, limit = 50) } returns ApiResponse(200, "OK", page1Data)
        coEvery { apiService.getUserList(search = "", page = 2, limit = 50) } throws RuntimeException("Timeout")

        val result = repository.syncUserData()
        assertTrue(result is Resource.Success)

        coVerify { userDao.updateData(match { it.size == 1 && it[0].id == "1" }) }
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
    fun `deleteUser should return Error when API returns failure status`() = runTest {
        val id = "1"
        val apiResponse = ApiResponse<Unit>(400, "Gagal menghapus user", Unit)

        coEvery { apiService.deleteUser(id) } returns apiResponse

        val result = repository.deleteUser(id)

        assertTrue(result is Resource.Error)
        assertEquals("Gagal menghapus user", (result as Resource.Error).errorMessage)

        coVerify(exactly = 0) { userDao.deleteUser(id) }
    }

    @Test
    fun `getUserDetail should return mapped domain model from DAO`() = runTest {
        val id = "123"
        val dummyEntity = UserEntity(
            id = "1",
            name = "Paneeyy",
            role = "petani",
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