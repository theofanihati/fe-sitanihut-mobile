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
        repository = ProfileRepositoryImpl(
            apiService = mockApiService,
            userDao = mockUserDao,
            roleDao = mockRoleDao,
            userPreferences = mockUserPreferences
        )
    }

    @Test
    fun `getUserDetail success from network should save to db and emit success`() = runTest {
        every { mockUserDao.getUserById(userId) } returnsMany listOf(
            flowOf(null),
            flowOf(dummyUserEntity)
        )

        coEvery { mockApiService.getUserDetail(userId) } returns ApiResponse(200, "ok", dummyUserDto)
        coEvery { mockRoleDao.getRoleName(roleId) } returns roleName
        coJustRun { mockUserDao.upsertUser(any()) }

        repository.getUserDetail(userId).test {
            assertTrue(awaitItem() is Resource.Loading)

            val successItem = awaitItem()
            assertTrue(successItem is Resource.Success)
            assertEquals(dummyUserDetail, successItem.data)
            assertEquals("Petani", successItem.data?.role)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { mockApiService.getUserDetail(userId) }
        coVerify { mockUserDao.upsertUser(any()) }
    }

    @Test
    fun `getUserDetail fail from network, then emit data cache`() = runTest {
        val cachedEntity = dummyUserEntity.copy(name = "Budi Lama")
        every { mockUserDao.getUserById(userId) } returns flowOf(cachedEntity)

        coEvery { mockApiService.getUserDetail(any()) } throws IOException("Tidak ada koneksi internet. Menampilkan data offline.")

        val flow = repository.getUserDetail(userId)

        flow.test {
            assertTrue(awaitItem() is Resource.Loading)

            val cacheItem = awaitItem()
            assertTrue(cacheItem is Resource.Success)
            assertEquals("Budi Lama", cacheItem.data?.name)

            val errorItem = awaitItem()
            assertTrue(errorItem is Resource.Error)
            assertEquals("Tidak ada koneksi internet. Menampilkan data offline.", errorItem.message)
            assertEquals("Budi Lama", errorItem.data?.name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when cache update success, should emit new data`() = runTest {
        val oldEntity = dummyUserEntity.copy(name = "Budi Lama")
        val newEntity = dummyUserEntity.copy(name = "Budi Baru")

        every { mockUserDao.getUserById(userId) } returnsMany listOf(
            flowOf(oldEntity),
            flowOf(newEntity)
        )

        val newDto = dummyUserDto.copy(name = "Budi Baru")
        coEvery { mockApiService.getUserDetail(userId) } returns ApiResponse( 200, "ok", newDto)

        coEvery { mockRoleDao.getRoleName(roleId) } returns roleName
        coJustRun { mockUserDao.upsertUser(any()) }

        val flow = repository.getUserDetail(userId)

        flow.test {
            assertTrue(awaitItem() is Resource.Loading)
            val oldItem = awaitItem()
            assertTrue(oldItem is Resource.Success)
            assertEquals("Budi Lama", oldItem.data?.name)

            val newItem = awaitItem()
            assertTrue(newItem is Resource.Success)
            assertEquals("Budi Baru", newItem.data?.name)

            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `when cache update fail, should emit error`() = runTest {
        every { mockUserDao.getUserById(userId) } returns flowOf(null)
        coEvery { mockApiService.getUserDetail(any()) } throws IOException("No Connection")

        val flow = repository.getUserDetail(userId)

        flow.test {
            assertTrue(awaitItem() is Resource.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is Resource.Error)
            assertNull(errorItem.data)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when roles empty, should fetch and insert roles`() = runTest {
        every { mockUserDao.getUserById(userId) } returnsMany listOf(
            flowOf(null),
            flowOf(dummyUserEntity)
        )
        coEvery { mockApiService.getUserDetail(any()) } returns ApiResponse(200, "ok", dummyUserDto)
        coJustRun { mockUserDao.upsertUser(any()) }

        coEvery { mockRoleDao.getRoleName(roleId) } returnsMany listOf(null, "Petani")

        val rolesResponse = ApiResponse( 200, "ok", listOf(RoleDto(roleId, "Petani")))
        coEvery { mockApiService.getRoles() } returns rolesResponse

        coJustRun { mockRoleDao.insertRoles(any()) }

        repository.getUserDetail(userId).test {
            awaitItem()

            val successItem = awaitItem()
            assertTrue(successItem is Resource.Success)
            assertEquals("Petani", successItem.data?.role)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { mockApiService.getRoles() }
        coVerify(exactly = 1) { mockRoleDao.insertRoles(any()) }
    }
}