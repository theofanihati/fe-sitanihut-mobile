package com.dishut_lampung.sitanihut.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    private lateinit var context: Context
    private lateinit var repository: ReportRepositoryImpl

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = ReportRepositoryImpl(apiService, db, reportDao, userPreferences, context)
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

    @Test
    fun `createReport should save to DB as PENDING_CREATE and enqueue Worker`() = runTest {
        val input = CreateReportInput(
            month = "Januari",
            period = 2024,
            modal = "1000000",
            plantingDetails = emptyList(),
            harvestDetails = emptyList(),
            farmerNotes = "Catatan",
            attachments = emptyList(),
            isAjukan = true,
            nte = 5000.0
        )
        coEvery { userPreferences.userId } returns flowOf("user-123")

        val reportSlot = slot<List<ReportEntity>>()
        coEvery { reportDao.insertAll(capture(reportSlot)) } returns Unit

        mockkStatic(WorkManager::class)
        val mockWorkManager = mockk<WorkManager>()
        every { WorkManager.getInstance(any()) } returns mockWorkManager
        every { mockWorkManager.enqueue(any<OneTimeWorkRequest>()) } returns mockk()

        val result = repository.createReport(input)
        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { reportDao.insertAll(any()) }
        val savedEntity = reportSlot.captured.first()

        assertEquals("user-123", savedEntity.userId)
        assertEquals(SyncStatus.PENDING_CREATE, savedEntity.syncStatus)
        assertNotNull(savedEntity.jsonPayload)

        verify(exactly = 1) { mockWorkManager.enqueue(any<OneTimeWorkRequest>()) }
        unmockkStatic(WorkManager::class)
    }

    @Test
    fun `createReport should return Error if Database insert fails`() = runTest {
        val input = mockk<CreateReportInput>(relaxed = true)
        coEvery { userPreferences.userId } returns flowOf("user-123")
        coEvery { reportDao.insertAll(any()) } throws Exception("Database Full")

        val result = repository.createReport(input)

        assertTrue(result is Resource.Error)
        assertEquals("Gagal menyimpan data: Database Full", result.message)
        mockkStatic(WorkManager::class)
        unmockkStatic(WorkManager::class)
    }
}