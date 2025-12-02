package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReportRepositoryImplTest {

    private val apiService: ReportApiService = mockk(relaxed = true)
    private val db: SitanihutDatabase = mockk(relaxed = true)
    private val reportDao: ReportDao = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)

    private lateinit var repository: ReportRepositoryImpl

    @Before
    fun setUp() {
        repository = ReportRepositoryImpl(apiService, db, reportDao, userPreferences)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `deleteReport should delete from local AND remote when token exists`() = runTest {
        val id = "123"
        coEvery { userPreferences.getAuthToken() } returns "fake_token"
        val result = repository.deleteReport(id)

        assertTrue(result is Resource.Success)
        coVerify { reportDao.deleteReportById(id) }
        coVerify { apiService.deleteReport(id) }
    }

    @Test
    fun `deleteReport should return Success even if API fails (Offline Logic)`() = runTest {
        val id = "123"
        coEvery { userPreferences.getAuthToken() } returns "fake_token"

        coEvery { apiService.deleteReport(id) } throws Exception("No Internet")

        val result = repository.deleteReport(id)
        assertTrue(result is Resource.Success)
        coVerify { reportDao.deleteReportById(id) }
    }

    @Test
    fun `submitReport should update local AND call API when token exists`() = runTest {
        val id = "123"
        coEvery { userPreferences.getAuthToken() } returns "fake_token"
        val result = repository.submitReport(id)

        assertTrue(result is Resource.Success)
        coVerify { reportDao.submitReportById(id) }
        coVerify { apiService.submitReport(eq(id), any(), any()) }
    }
}