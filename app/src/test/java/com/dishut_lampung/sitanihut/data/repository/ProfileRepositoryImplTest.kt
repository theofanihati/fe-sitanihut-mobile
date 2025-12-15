package com.dishut_lampung.sitanihut.data.repository

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.RoleDao
import com.dishut_lampung.sitanihut.data.local.dao.UserDao
import com.dishut_lampung.sitanihut.data.local.entity.RoleEntity
import com.dishut_lampung.sitanihut.data.local.entity.UserEntity
import com.dishut_lampung.sitanihut.data.mapper.toUserDetail
import com.dishut_lampung.sitanihut.data.remote.api.UserApiService
import com.dishut_lampung.sitanihut.data.remote.dto.RoleDto
import com.dishut_lampung.sitanihut.data.remote.dto.UserDetailDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.domain.repository.ProfileRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ProfileRepositoryImplTest {

    private lateinit var mockApiService: UserApiService
    private lateinit var mockUserDao: UserDao
    private lateinit var mockRoleDao: RoleDao
    private lateinit var mockUserPreferences: UserPreferences
    private lateinit var repository: ProfileRepository

    private val userId = "123"
    private val roleId = "role-uuid-123"
    private val roleName = "Petani"
    private val dummyUserDto = UserDetailDto(
        id = userId,
        roleId = roleId,
        name = "Budi Petani",
        email = "budi@tani.com",
        profilePictureUrl = "http://img.com/1.jpg",
        kphId = "kph-1", kphName = "KPH A", kthId = "kth-1", kthName = "KTH B",
        identityNumber = "12345", gender = "L", address = "Desa A",
        whatsAppNumber = "08123", lastEducation = "SMA", sideJob = "-",
        landArea = 1.5, position = "Anggota"
    )

    private val dummyUserEntity = UserEntity(
        id = userId,
        name = "Budi Petani",
        role = roleName,
        profilePictureUrl = "http://img.com/1.jpg",
        roleId = roleId,
        email = "budi@tani.com",
        address = "Desa A"
    )

    private val dummyUserDetail = dummyUserEntity.toUserDetail()

    @Before
    fun setUp() {
        mockApiService = mockk()
        mockUserDao = mockk()
        mockRoleDao = mockk()
        mockUserPreferences = mockk(relaxed = true)
        repository = ProfileRepositoryImpl(
            apiService = mockApiService,
            userDao = mockUserDao,
            roleDao = mockRoleDao,
            userPreferences = mockUserPreferences
        )
    }

    @Test
    fun `getUserDetail should emit Loading when data is null`() = runTest {
        every { mockUserDao.getUserById(userId) } returns flowOf(null)
        repository.getUserDetail(userId).test {
            val item = awaitItem()
            assertTrue(item is Resource.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getUserDetail should emit Success when data exists in DB`() = runTest {
        every { mockUserDao.getUserById(userId) } returns flowOf(dummyUserEntity)
        repository.getUserDetail(userId).test {
            val item = awaitItem()
            assertTrue(item is Resource.Success)
            assertEquals(dummyUserDetail, item.data)
            assertEquals("Petani", item.data?.role)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when syncUserDetail success, should fetch api and upsert to db`() = runTest {
        every { mockUserPreferences.userId } returns flowOf(userId)
        coEvery { mockApiService.getUserDetail(userId) } returns ApiResponse(200, "ok", dummyUserDto)
        coEvery { mockRoleDao.getRoleName(roleId) } returns roleName
        coJustRun { mockUserDao.upsertUser(any()) }

        val result = repository.syncUserDetail()
        assertTrue(result is Resource.Success)

        coVerify { mockApiService.getUserDetail(userId) }
        coVerify { mockUserDao.upsertUser(match { it.id == userId && it.name == "Budi Petani" }) }
        coVerify { mockUserPreferences.saveUserName("Budi Petani") }
    }

    @Test
    fun `syncUserDetail should return Error when userId is null in preferences`() = runTest {
        every { mockUserPreferences.userId } returns flowOf(null)

        val result = repository.syncUserDetail()

        assertTrue(result is Resource.Error)
        assertEquals("No User ID", result.message)

        coVerify(exactly = 0) { mockApiService.getUserDetail(any()) }
    }

    @Test
    fun `syncUserDetail should NOT save avatar when profilePictureUrl is null or empty`() = runTest {
        val userNoAvatar = dummyUserDto.copy(profilePictureUrl = "")

        every { mockUserPreferences.userId } returns flowOf(userId)
        coEvery { mockApiService.getUserDetail(userId) } returns ApiResponse(200, "ok", userNoAvatar)
        coEvery { mockRoleDao.getRoleName(any()) } returns roleName
        coJustRun { mockUserDao.upsertUser(any()) }
        coJustRun { mockUserPreferences.saveUserName(any()) }

        val result = repository.syncUserDetail()
        assertTrue(result is Resource.Success)
        coVerify { mockUserPreferences.saveUserName("Budi Petani") }

        coVerify(exactly = 0) { mockUserPreferences.saveUserAvatar(any()) }
    }

    @Test
    fun `when syncUserDetail fail, should return error`() = runTest {
        every { mockUserPreferences.userId } returns flowOf(userId)
        coEvery { mockApiService.getUserDetail(userId) } throws IOException("No Internet")

        val result = repository.syncUserDetail()
        assertTrue(result is Resource.Error)
        assertTrue(result.message!!.contains("Gagal sinkronisasi"))
    }

    @Test
    fun `when roles null locally, syncUserDetail should fetch roles`() = runTest {
        every { mockUserPreferences.userId } returns flowOf(userId)
        coEvery { mockApiService.getUserDetail(userId) } returns ApiResponse(200, "ok", dummyUserDto)
        coEvery { mockRoleDao.getRoleName(roleId) } returnsMany listOf(null, "Petani")

        val rolesResponse = ApiResponse(200, "ok", listOf(RoleDto(roleId, "Petani")))
        coEvery { mockApiService.getRoles() } returns rolesResponse
        coJustRun { mockRoleDao.insertRoles(any()) }
        coJustRun { mockUserDao.upsertUser(any()) }

        val result = repository.syncUserDetail()
        assertTrue(result is Resource.Success)

        coVerify { mockApiService.getRoles() }
        coVerify { mockRoleDao.insertRoles(any()) }
        coVerify { mockUserDao.upsertUser(match { it.role == "Petani" }) }
    }

    @Test
    fun `syncUserDetail should use fallback role 'Pengguna' when API fails and local data missing`() = runTest {
        every { mockUserPreferences.userId } returns flowOf(userId)
        coEvery { mockApiService.getUserDetail(userId) } returns ApiResponse(200, "ok", dummyUserDto)
        coEvery { mockRoleDao.getRoleName(roleId) } returns null
        coEvery { mockApiService.getRoles() } throws RuntimeException("API Error")
        every { mockUserPreferences.userRole } returns flowOf(null)

        coJustRun { mockUserDao.upsertUser(any()) }
        coJustRun { mockUserPreferences.saveUserName(any()) }
        coJustRun { mockUserPreferences.saveUserAvatar(any()) }

        val result = repository.syncUserDetail()
        assertTrue(result is Resource.Success)
        coVerify {
            mockUserDao.upsertUser(match {
                it.role == "Pengguna"
            })
        }
    }
}