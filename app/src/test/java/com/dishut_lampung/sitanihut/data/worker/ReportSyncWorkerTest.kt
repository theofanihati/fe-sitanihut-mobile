package com.dishut_lampung.sitanihut.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.data.remote.dto.ConflictResponseDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.google.gson.Gson
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import retrofit2.Response
import java.io.IOException
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class ReportSyncWorkerTest {

    private lateinit var context: Context
    private val apiService: ReportApiService = mockk()
    private val reportDao: ReportDao = mockk()

    val gson = Gson()

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    private fun buildWorker(): ReportSyncWorker {
        return TestListenableWorkerBuilder<ReportSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker {
                    return ReportSyncWorker(
                        appContext,
                        workerParameters,
                        apiService,
                        reportDao
                    )
                }
            })
            .build()
    }

    @Test
    fun `doWork should return Success when no pending reports`() = runTest {
        coEvery { reportDao.getAllUnsyncedReports() } returns emptyList()
        val worker = buildWorker()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `syncCreate success should update status to SYNCED`() = runTest {
        val reportId = UUID.randomUUID().toString()
        val pendingReport = createReportEntity(reportId, SyncStatus.PENDING_CREATE)
        coEvery { reportDao.getAllUnsyncedReports() } returns listOf(pendingReport)
        coEvery { reportDao.upsertAll(any()) } just Runs

        val successResponse = ApiResponse<Any?>(statusCode = 200, message = "OK", data = Any())
        coEvery { apiService.createReport(any()) } returns Response.success(successResponse)
        val worker = buildWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) {
            reportDao.upsertAll(
                match { list ->
                    list.size == 1 &&
                            list.first().id == reportId &&
                            list.first().syncStatus == SyncStatus.SYNCED
                }
            )
        }
    }

    @Test
    fun `syncCreate should handle 409 conflict by retrying with new ID`() = runTest {
        val oldId = UUID.randomUUID().toString()
        val newId = UUID.randomUUID().toString()
        val oldPayload = """{
            |"id": "$oldId", 
            |"period": 2024, 
            |"bulan": "Januari",
            |"modal": 0.0,
            |"nte": 0.0,
            |"catatan_petani":"",
            |"status": "menunggu",
            | "masa_tanam":[],
            | "masa_panen":[],
            |"updated_at": ""}""".trimMargin()
        val pendingReport = createReportEntity(oldId, SyncStatus.PENDING_CREATE, oldPayload)

        coEvery { reportDao.getAllUnsyncedReports() } returns listOf(pendingReport)
        coEvery { reportDao.upsertAll(any()) } just Runs

        val conflictDto = ConflictResponseDto(
            newUuid = newId,
            error = "Resource conflict, new ID assigned")
        val errorBody = gson.toJson(conflictDto).toResponseBody("application/json".toMediaTypeOrNull())
        val conflictResponse = Response.error<ApiResponse<Any?>>(409, errorBody)
        coEvery { apiService.createReport(any()) } returns conflictResponse

        val worker = buildWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify(exactly = 1) { reportDao.deleteReportById(oldId) }
        coVerify(exactly = 1) {
            reportDao.upsertAll(
                match { list ->
                    val newEntity = list.first()
                    list.size == 1 &&
                            newEntity.id == newId &&
                            newEntity.syncStatus == SyncStatus.PENDING_CREATE
                }
            )
        }
    }

    @Test
    fun `syncUpdate success should update status to SYNCED`() = runTest {
        val reportId = UUID.randomUUID().toString()
        val pendingReport = createReportEntity(
            reportId,
            SyncStatus.PENDING_UPDATE,
            jsonPayload = """{"id": "$reportId", "status": "disetujui", "modal": 500}"""
        )
        coEvery { reportDao.getAllUnsyncedReports() } returns listOf(pendingReport)
        coEvery { reportDao.upsertAll(any()) } just Runs

        val updateSuccessResponse = ApiResponse(statusCode = 200, message = "OK", data = Unit)
        coEvery { apiService.updateReport(eq(reportId), any()) } returns Response.success(updateSuccessResponse)

        val worker = buildWorker()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) { apiService.updateReport(eq(reportId), any()) }
        coVerify(exactly = 1) {
            reportDao.upsertAll(
                match { list ->
                    list.first().syncStatus == SyncStatus.SYNCED &&
                            list.first().jsonPayload == null
                }
            )
        }
    }

    @Test
    fun `syncDelete success should remove report from local db`() = runTest {
        val reportId = UUID.randomUUID().toString()
        val pendingReport = createReportEntity(reportId, SyncStatus.PENDING_DELETE, jsonPayload = null)
        coEvery { reportDao.getAllUnsyncedReports() } returns listOf(pendingReport)
        coEvery { reportDao.deleteReportById(any()) } just Runs

        val successDeleteResponse = ApiResponse<Any?>(statusCode = 200, message = "OK", data = Any())
        coEvery { apiService.deleteReport(eq(reportId)) } returns Response.success(successDeleteResponse)
        val worker = buildWorker()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) { reportDao.deleteReportById(eq(reportId)) }
    }

    @Test
    fun `syncDelete should remove report if server returns 404 (already deleted)`() = runTest {
        val reportId = UUID.randomUUID().toString()
        val pendingReport = createReportEntity(reportId, SyncStatus.PENDING_DELETE, jsonPayload = null)
        coEvery { reportDao.getAllUnsyncedReports() } returns listOf(pendingReport)
        coEvery { reportDao.deleteReportById(any()) } just Runs

        val errorBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        val notFoundResponse = Response.error<ApiResponse<Any?>>(404, errorBody)
        coEvery { apiService.deleteReport(eq(reportId)) } returns notFoundResponse

        val worker = buildWorker()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) { reportDao.deleteReportById(eq(reportId)) }
    }

    @Test
    fun `doWork should return Retry if API call throws IOException`() = runTest {
        val reportId = UUID.randomUUID().toString()
        val pendingReport = createReportEntity(reportId, SyncStatus.PENDING_CREATE)

        coEvery { reportDao.getAllUnsyncedReports() } returns listOf(pendingReport)
        coEvery { apiService.createReport(any()) } throws IOException("No internet")

        val worker = buildWorker()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.retry(), result)

        coVerify(exactly = 0) { reportDao.upsertAll(any()) }
        coVerify(exactly = 0) { reportDao.deleteReportById(any()) }
    }

    @Test
    fun `doWork should return Retry if API returns unsuccessful status code other than 409 or 404`() = runTest {
        val reportId = UUID.randomUUID().toString()
        val pendingReport = createReportEntity(reportId, SyncStatus.PENDING_UPDATE)
        coEvery { reportDao.getAllUnsyncedReports() } returns listOf(pendingReport)

        val errorBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        val serverError = Response.error<ApiResponse<Unit>>(500, errorBody)
        coEvery { apiService.updateReport(any(), any()) } returns serverError

        val worker = buildWorker()
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.retry(), result)

        coVerify(exactly = 0) { reportDao.upsertAll(any()) }
    }
}

private fun createReportEntity(
    id: String,
    status: SyncStatus,
    jsonPayload: String? = """{"id": "$id", "period": 2024, "status": "menunggu"}""",
    attachmentsJson: String = "[]"
): ReportEntity {
    return ReportEntity(
        id = id,
        period = 2024,
        month = "Jan",
        syncStatus = status,
        jsonPayload = jsonPayload,
        attachmentsJson = attachmentsJson,
        date = "2024-01-01",
        nte = 1000.0,
        userId = "user1",
        userAddress = "",
        userGender = "",
        userName = "Budi",
        userNik = "",
        userKphName = "",
        userKthName = "",
        status = "menunggu",
        modal = 0.0,
        farmerNotes = "",
        plantingDetailsJson = "[]",
        harvestDetailsJson = "[]",
        createdAt = "",
    )
}