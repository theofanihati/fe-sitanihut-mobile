package com.dishut_lampung.sitanihut.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.data.remote.dto.ReportDetailDto
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.model.ReportAttachment
import com.dishut_lampung.sitanihut.util.Resource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.first
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
import retrofit2.HttpException

@RunWith(RobolectricTestRunner::class)
class ReportRepositoryImplTest {

    private val apiService: ReportApiService = mockk(relaxed = true)
    private val db: SitanihutDatabase = mockk(relaxed = true)
    private val reportDao: ReportDao = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private lateinit var context: Context
    private lateinit var repository: ReportRepositoryImpl

    private val mockWorkManager: WorkManager = mockk()
    private val mockOperation: Operation = mockk(relaxed = true)

    val dummyReport = ReportEntity(
        id = "123",
        userId = "user1",
        period = 2024,
        month = "Februari",
        date = "2024-02-01",
        nte = 100000.0,
        status = "menunggu",
        syncStatus = SyncStatus.SYNCED,
        jsonPayload = null,
        plantingDetailsJson = "[]",
        harvestDetailsJson = "[]",
    )

    val detailDto = ReportDetailDto(
        id = "id-1",
        userId = "user1",
        userAddress = "",
        userGender = "",
        userName = "Budi",
        userNik = "",
        userKphName = "",
        userKthName = "",
        period = 2024,
        month = "Jan",
        date = "2024-01-01",
        nte = 1000.0,
        status = "menunggu",
        modal = 0.0,
        farmerNotes = "",
        plantingDetails = emptyList(),
        harvestDetails = emptyList(),
        attachments = emptyList(),
        penyuluhNotes = "",
        createdAt = "2024-01-01T00:00:00Z",
        acceptedAt = "2024-01-01T00:00:00Z",
        verifiedAt = "2024-01-01T00:00:00Z"
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = ReportRepositoryImpl(apiService, db, reportDao, userPreferences, context)

        every { db.reportDao() } returns reportDao
        every { userPreferences.userId } returns flowOf("user-123")
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns mockWorkManager
        every {
            mockWorkManager.enqueueUniqueWork(
                any(),
                any(),
                any<OneTimeWorkRequest>()
            )
        } returns mockOperation
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `enqueueReportOperation should create correct WorkRequest`() = runTest {
        val opType = "CREATE"
        val reportId = "rep-123"
        val slotRequest = slot<OneTimeWorkRequest>()

        every {
            mockWorkManager.enqueueUniqueWork(
                any(),
                any(),
                capture(slotRequest)
            )
        } returns mockOperation

        repository.enqueueReportOperation(opType, reportId)

        val workRequest = slotRequest.captured
        val workSpec = workRequest.workSpec
        val inputData = workSpec.input
        assertEquals(opType, inputData.getString("OPERATION_TYPE"))
        assertEquals(reportId, inputData.getString("REPORT_ID"))
        assertEquals(androidx.work.NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
    }

    @Test
    fun `getReports should return PagingData flow`() = runTest {
        val query = "test"
        every { reportDao.getReports(query, any()) } returns mockk(relaxed = true)

        val resultFlow = repository.getReports(query, null)
        assertNotNull(resultFlow)
    }

    @Test
    fun `deleteReport should mark local as PENDING_DELETE and enqueue Worker`() = runTest {
        val id = "123"
        coEvery { reportDao.getSyncStatus(id) } returns SyncStatus.SYNCED

        val result = repository.deleteReport(id)
        assertTrue(result is Resource.Success)

        coVerify { reportDao.markAsPendingDelete(id) }
        verify {
            mockWorkManager.enqueueUniqueWork(
                "sync_report_queue",
                ExistingWorkPolicy.APPEND,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `unsynced item should just delete locally`() = runTest {
        val id = "123"
        coEvery { reportDao.getSyncStatus(id) } returns SyncStatus.PENDING_CREATE

        val result = repository.deleteReport(id)
        assertTrue(result is Resource.Success)
        coVerify { reportDao.deleteReportById(id) }
        verify(exactly = 0) { mockWorkManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `submitReport should update local AND enqueue Worker`() = runTest {
        val id = "123"
        coEvery { reportDao.getReportById(id) } returns dummyReport
        coJustRun { reportDao.upsertAll(any()) }

        val result = repository.submitReport(id)

        assertTrue(result is Resource.Success)
        coVerify {
            reportDao.upsertAll(match { list ->
                list.first().status == "menunggu" &&
                        list.first().syncStatus == SyncStatus.PENDING_UPDATE
            })
        }
        verify {
            mockWorkManager.enqueueUniqueWork(
                "sync_report_queue",
                ExistingWorkPolicy.APPEND,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `submitReport should return Error when report not found`() = runTest {
        val id = "unknown-id"
        coEvery { reportDao.getReportById(id) } returns null

        val result = repository.submitReport(id)

        assertTrue(result is Resource.Error)
        assertEquals("Laporan tidak ditemukan di database lokal", result.message)
    }

    @Test
    fun `submitReport should return Error when exception occurs`() = runTest {
        val id = "123"
        coEvery { reportDao.getReportById(id) } throws Exception("Unexpected Error")

        val result = repository.submitReport(id)

        assertTrue(result is Resource.Error)
        assertTrue(result.message!!.contains("Gagal mengajukan laporan"))
    }

    @Test
    fun `deleteReport should return Error when exception occurs`() = runTest {
        val id = "123"
        coEvery { reportDao.getSyncStatus(id) } throws Exception("DB Error")

        val result = repository.deleteReport(id)

        assertTrue(result is Resource.Error)
        assertTrue(result.message!!.contains("Gagal menghapus data"))
    }

    @Test
    fun `updateReport should return Error when exception occurs`() = runTest {
        val id = "123"
        val input = mockk<CreateReportInput>(relaxed = true)
        coEvery { reportDao.getReportById(id) } throws RuntimeException("Calculation Error")

        val result = repository.updateReport(id, input)

        assertTrue(result is Resource.Error)
        assertTrue(result.message!!.contains("Gagal menyimpan perubahan"))
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
            newAttachments = listOf("/local/path/img.jpg"),
            existingAttachmentIds = emptyList(),
            isAjukan = true,
            nte = 5000.0
        )

        val reportSlot = slot<List<ReportEntity>>()
        coEvery { reportDao.upsertAll(capture(reportSlot)) } just Runs

        val result = repository.createReport(input)
        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { reportDao.upsertAll(any()) }
        val savedEntity = reportSlot.captured.first()

        assertEquals("user-123", savedEntity.userId)
        assertEquals(SyncStatus.PENDING_CREATE, savedEntity.syncStatus)
        assertEquals("menunggu", savedEntity.status)

        verify(exactly = 1) {
            mockWorkManager.enqueueUniqueWork("sync_report_queue", ExistingWorkPolicy.APPEND, any<OneTimeWorkRequest>())
        }
        unmockkStatic(WorkManager::class)
    }

    @Test
    fun `createReport should return Error if Database insert fails`() = runTest {
        val input = mockk<CreateReportInput>(relaxed = true)
        coEvery { reportDao.upsertAll(any()) } throws Exception("Database Full")

        val result = repository.createReport(input)

        assertTrue(result is Resource.Error)
        assertEquals("Gagal menyimpan data: Database Full", result.message)
        verify(exactly = 0) { mockWorkManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `createReport should save as belum diajukan and NOT enqueue Worker if not submitted`() = runTest {
        val input = CreateReportInput(
            month = "Maret",
            period = 2025,
            modal = "500000",
            plantingDetails = emptyList(),
            harvestDetails = emptyList(),
            farmerNotes = "Draf Laporan",
            newAttachments = emptyList(),
            existingAttachmentIds = emptyList(),
            isAjukan = false,
            nte = 0.0
        )

        val reportSlot = slot<List<ReportEntity>>()
        coEvery { reportDao.upsertAll(capture(reportSlot)) } just Runs

        val result = repository.createReport(input)
        assertTrue(result is Resource.Success)

        val savedEntity = reportSlot.captured.first()

        assertEquals("belum diajukan", savedEntity.status)
        assertEquals(SyncStatus.PENDING_CREATE, savedEntity.syncStatus)

        verify(exactly = 0) {
            mockWorkManager.enqueueUniqueWork("sync_report_queue", ExistingWorkPolicy.APPEND, any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun `getReportById should emit Success when data found`() = runTest {
        val id = "123"
        coEvery { apiService.getReportDetail(id) } throws Exception("Skip network check")
        every { reportDao.getReportByIdFlow(id=id) } returns flowOf(dummyReport)

        val result = repository.getReportById(id).first { it !is Resource.Loading }
        assertTrue(result is Resource.Success)
        assertNotNull(result.data)
        assertEquals("Februari", result.data?.month)
    }

    @Test
    fun `getReportById should emit Error when data not found`() = runTest {
        val id = "999"
        every { reportDao.getReportByIdFlow(id) } returns flowOf(null)

        val result = repository.getReportById(id).first { it !is Resource.Loading }
        assertTrue(result is Resource.Error)
        assertEquals("Laporan tidak ditemukan", result.message)
    }

    @Test
    fun `getReportById should fetch network detail and save to DB before returning flow`() = runTest {
        val id = "id-1"
        coEvery { apiService.getReportDetail(id) } returns ApiResponse(statusCode = 200, message = "OK", data = detailDto)
        every { reportDao.getReportByIdFlow(id) } returns flowOf(dummyReport)
        coJustRun { reportDao.upsertAll(any()) }

        val result = repository.getReportById(id).first { it is Resource.Success }
        assertTrue(result is Resource.Success)
        coVerify { reportDao.upsertAll(match { it.first().id == id }) }
    }

    @Test
    fun `getReportById should handle Network HttpException and return local flow`() = runTest {
        val id = "id-1"
        coEvery { apiService.getReportDetail(id) } throws HttpException(mockk(relaxed = true) {
            every { code() } returns 404
        })

        every { reportDao.getReportByIdFlow(id) } returns flowOf(dummyReport)

        val result = repository.getReportById(id).first { it is Resource.Success }
        assertTrue(result is Resource.Success)
        coVerify(exactly = 0) { reportDao.upsertAll(any()) }
    }

    @Test
    fun `getReportById should return local flow after network fetch fails with HttpException`() = runTest {
        val id = "id-1"
        val mockResponse: retrofit2.Response<ApiResponse<ReportDetailDto>> = mockk(relaxed = true)
        every { mockResponse.isSuccessful } returns false
        coEvery { apiService.getReportDetail(id) } throws HttpException(mockResponse)
        every { reportDao.getReportByIdFlow(id) } returns flowOf(dummyReport)

        val result = repository.getReportById(id).first { it !is Resource.Loading }
        assertTrue(result is Resource.Success)
        assertEquals("Februari", result.data?.month)
        coVerify(exactly = 0) { reportDao.upsertAll(any()) } // Detail gagal disimpan
    }

    @Test
    fun `updateReport should keep PENDING_CREATE status when updating unsynced report`() = runTest {
        val id = "123"
        val oldEntity = ReportEntity(
            id = id,
            userId = "user1",
            period = 2024,
            month = "Januari",
            date = "2024-01-01",
            nte = 0.0,
            status = "menunggu",
            syncStatus = SyncStatus.PENDING_CREATE,
            jsonPayload = null
        )

        val input = CreateReportInput(
            month = "Januari",
            period = 2024,
            modal = "5000",
            farmerNotes = "Catatan",
            nte = 100000.0,
            isAjukan = false,
            newAttachments = emptyList(),
            existingAttachmentIds = emptyList(),
            plantingDetails = emptyList(),
            harvestDetails = emptyList()
        )

        coEvery { reportDao.getReportById(id) } returns oldEntity
        coEvery { reportDao.upsertAll(any()) } just Runs

        val result = repository.updateReport(id, input)
        assertTrue(result is Resource.Success)

        coVerify {
            reportDao.upsertAll(withArg { list ->
                val entity = list.first()

                assertEquals(SyncStatus.PENDING_CREATE, entity.syncStatus)
                assertNotNull(entity.jsonPayload)
                assertEquals("Catatan", entity.farmerNotes)
            })
        }
    }

    @Test
    fun `updateReport should change status to PENDING_UPDATE when updating synced report`() = runTest {
        val id = "123"
        val oldEntity = ReportEntity(
            id = id,
            userId = "user1",
            period = 2024,
            month = "Januari",
            date = "2024-01-01",
            nte = 100000.0,
            status = "menunggu",
            syncStatus = SyncStatus.SYNCED,
            jsonPayload = null
        )
        val input = CreateReportInput(
            month = "Februari",
            period = 2024,
            modal = "6000",
            farmerNotes = "Edit Catatan",
            nte = 200000.0,
            isAjukan = false,
            newAttachments = emptyList(),
            existingAttachmentIds = emptyList(),
            plantingDetails = emptyList(),
            harvestDetails = emptyList()
        )

        coEvery { reportDao.getReportById(id) } returns oldEntity
        coEvery { reportDao.upsertAll(any()) } just Runs

        val result = repository.updateReport(id, input)

        assertTrue(result is Resource.Success)

        coVerify {
            reportDao.upsertAll(withArg { list ->
                val entity = list.first()
                assertEquals(SyncStatus.PENDING_UPDATE, entity.syncStatus)
                assertNotNull(entity.jsonPayload)
                assertEquals("Februari", entity.month)
            })
        }
    }

    @Test
    fun `updateReport should update synced report to DRAFT and set PENDING_UPDATE status`() = runTest {
        val id = "456"
        val oldEntity = dummyReport.copy(
            id = id,
            syncStatus = SyncStatus.SYNCED,
            status = "diterima"
        )
        val input = CreateReportInput(
            month = "Februari",
            period = 2024,
            modal = "7000",
            farmerNotes = "Revisi Draft",
            nte = 150000.0,
            isAjukan = false,
            newAttachments = emptyList(),
            existingAttachmentIds = emptyList(),
            plantingDetails = emptyList(),
            harvestDetails = emptyList()
        )

        coEvery { reportDao.getReportById(id) } returns oldEntity
        coEvery { reportDao.upsertAll(any()) } just Runs

        val result = repository.updateReport(id, input)

        assertTrue(result is Resource.Success)

        coVerify {
            reportDao.upsertAll(withArg { list ->
                val entity = list.first()
                assertEquals("belum diajukan", entity.status) // Status changed to draft
                assertEquals(SyncStatus.PENDING_UPDATE, entity.syncStatus) // Sync status updated
                assertNotNull(entity.jsonPayload)
            })
        }
        verify(exactly = 1) { mockWorkManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `updateReport should handle attachment changes correctly and include existing IDs`() = runTest {
        val id = "789"
        val oldEntity = dummyReport.copy(
            id = id,
            syncStatus = SyncStatus.SYNCED,
            attachmentsJson = """[{"id":"att-old", "filePath": "server/path", "isLocal": false}]"""
        )

        val input = CreateReportInput(
            month = "April",
            period = 2024,
            modal = "0",
            farmerNotes = "Testing Attachments",
            nte = 0.0,
            isAjukan = true,
            newAttachments = listOf("/local/img1.jpg", "/local/doc1.pdf"),
            existingAttachmentIds = listOf("att-old", "att-new-existing"),
            plantingDetails = emptyList(),
            harvestDetails = emptyList()
        )

        coEvery { reportDao.getReportById(id) } returns oldEntity
        coEvery { reportDao.upsertAll(any()) } just Runs

        repository.updateReport(id, input)

        coVerify {
            reportDao.upsertAll(withArg { list ->
                val entity = list.first()
                val attachmentsType = object : TypeToken<List<ReportAttachment>>() {}.type
                val attachments = Gson().fromJson<List<ReportAttachment>>(entity.attachmentsJson, attachmentsType)
                assertEquals(4, attachments.size)
                assertTrue(attachments.any { it.id == "att-old" && !it.isLocal })
                assertTrue(attachments.any { it.id == "att-new-existing" && !it.isLocal })
                assertTrue(attachments.any { it.filePath.contains("img1.jpg") && it.isLocal })
            })
        }
    }
    @Test
    fun `updateReport should return Error when report not found`() = runTest {
        val id = "999"
        val input = mockk<CreateReportInput>(relaxed = true)

        coEvery { reportDao.getReportById(id) } returns null
        val result = repository.updateReport(id, input)

        assertTrue(result is Resource.Error)
        assertEquals("Laporan tidak ditemukan", result.message)

        coVerify(exactly = 0) { reportDao.upsertAll(any()) }
    }

    @Test
    fun `syncReportDetail success should fetch list, save to DB, and fetch details for top 10`() = runTest {
        val listDto = (1..12).map { i ->
            ReportListItemDto(
                id = "id-$i",
                date = "2024-01-01",
                period = 2024,
                month = "Januari",
                nte = 1000.0,
                status = "menunggu",
                userId = "user1",
                userName = "Budi"
            )
        }
        val listResponse = ApiResponse(
            statusCode = 200,
            message = "OK",
            data = PaginatedData(
                count = 12, totalPages = 1, data = listDto
            )
        )

        coEvery { apiService.getLatestReports() } returns listResponse
        coEvery { apiService.getReportDetail(any()) } returns ApiResponse(statusCode = 200, message = "OK", data = detailDto)

        coJustRun { reportDao.upsertAll(any()) }

        val result = repository.syncReportDetail()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { reportDao.upsertAll(match { it.size == 12 }) }
        coVerify(exactly = 10) { apiService.getReportDetail(any()) }
        coVerify(atLeast = 10) { reportDao.upsertAll(match { it.size == 1 }) }
    }

    @Test
    fun `syncReportDetail failure should return Error`() = runTest {
        coEvery { apiService.getLatestReports() } throws Exception("Network Error")

        val result = repository.syncReportDetail()

        assertTrue(result is Resource.Error)
        assertTrue(result.message!!.contains("Gagal sinkronisasi"))
    }

    @Test
    fun `syncReportDetail should return Success when API returns empty list`() = runTest {
        val emptyListResponse = ApiResponse<PaginatedData<ReportListItemDto>>(
            statusCode = 200,
            message = "OK",
            data = PaginatedData(count = 0, totalPages = 0, data = emptyList())
        )

        coEvery { apiService.getLatestReports() } returns emptyListResponse
        coJustRun { reportDao.upsertAll(any()) }

        val result = repository.syncReportDetail()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 0) { reportDao.upsertAll(match { it.isEmpty() }) }
        coVerify(exactly = 0) { apiService.getReportDetail(any()) }
    }

    @Test
    fun `syncReportDetail should return Error when list fetch fails with HttpException`() = runTest {
        val mockResponse: retrofit2.Response<ApiResponse<PaginatedData<ReportListItemDto>>> = mockk(relaxed = true)
        every { mockResponse.isSuccessful } returns false
        coEvery { apiService.getLatestReports() } throws HttpException(mockResponse)

        val result = repository.syncReportDetail()

        assertTrue(result is Resource.Error)
        assertTrue(result.message!!.contains("Gagal sinkronisasi"))
    }

    @Test
    fun `syncReportDetail should continue on detail fetch error for other items`() = runTest {
        val listDto = (1..3).map { i ->
            ReportListItemDto(
                id = "id-$i",
                date = "2024-01-01",
                period = 2024,
                month = "Januari",
                nte = 1000.0,
                status = "menunggu",
                userId = "user1",
                userName = "Budi"
            )
        }
        val listResponse = ApiResponse(
            statusCode = 200,
            message = "OK",
            data = PaginatedData(count = 3, totalPages = 1, data = listDto)
        )

        coEvery { apiService.getLatestReports() } returns listResponse
        coEvery { apiService.getReportDetail("id-1") } returns ApiResponse(statusCode = 200, message = "OK", data = detailDto.copy(id = "id-1"))
        coEvery { apiService.getReportDetail("id-2") } throws Exception("Detail Not Found Error") // Failure on one detail fetch
        coEvery { apiService.getReportDetail("id-3") } returns ApiResponse(statusCode = 200, message = "OK", data = detailDto.copy(id = "id-3"))

        coJustRun { reportDao.upsertAll(any()) }

        val result = repository.syncReportDetail()
        assertTrue(result is Resource.Success)

        coVerify(exactly = 1) { reportDao.upsertAll(match { it.size == 3 }) }
        coVerify(exactly = 3) { apiService.getReportDetail(any()) }
        coVerify(exactly = 2) { reportDao.upsertAll(match { it.size == 1 }) }
    }

}