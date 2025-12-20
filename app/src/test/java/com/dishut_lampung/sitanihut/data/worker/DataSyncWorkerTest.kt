package com.dishut_lampung.sitanihut.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.repository.CommodityRepository
import com.dishut_lampung.sitanihut.domain.repository.PenyuluhRepository
import com.dishut_lampung.sitanihut.domain.repository.ProfileRepository
import com.dishut_lampung.sitanihut.domain.repository.ReportRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataSyncWorkerTest {

    private lateinit var context: Context
    private val mockCommodityRepo: CommodityRepository = mockk(relaxed = true)
    private val mockProfileRepo: ProfileRepository = mockk(relaxed = true)
    private val mockReportRepo: ReportRepository = mockk(relaxed = true)
    private val mockPenyuluhRepository: PenyuluhRepository = mockk(relaxed = true)
    private val mockUserPreferences: UserPreferences = mockk(relaxed = true)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `doWork should call all repositories and update preference on success`() = runTest {
        coEvery { mockProfileRepo.syncUserDetail() } returns Resource.Success(Unit)
        coEvery { mockCommodityRepo.syncCommodities() } returns Resource.Success(Unit)
        coEvery { mockReportRepo.syncReportDetail() } returns Resource.Success(Unit)
        coJustRun { mockUserPreferences.updateLastSyncTime() }

        val worker = TestListenableWorkerBuilder<DataSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker {
                    return DataSyncWorker(
                        appContext,
                        workerParameters,
                        mockCommodityRepo,
                        mockProfileRepo,
                        mockReportRepo,
                        mockPenyuluhRepository,
                        mockUserPreferences,
                    )
                }
            })
            .build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)

        coVerify(exactly = 1) { mockProfileRepo.syncUserDetail() }
        coVerify(exactly = 1) { mockCommodityRepo.syncCommodities() }
        coVerify(exactly = 1) { mockReportRepo.syncReportDetail() }
        coVerify(exactly = 1) { mockUserPreferences.updateLastSyncTime() }
        coVerify(exactly = 1) { mockPenyuluhRepository.syncPenyuluhData() }
    }

    @Test
    fun `doWork should return failure when an exception occurs`() = runTest {
        coEvery { mockProfileRepo.syncUserDetail() } throws Exception("Fatal API Error")

        val worker = TestListenableWorkerBuilder<DataSyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker {
                    return DataSyncWorker(
                        appContext,
                        workerParameters,
                        mockCommodityRepo,
                        mockProfileRepo,
                        mockReportRepo,
                        mockPenyuluhRepository,
                        mockUserPreferences,
                    )
                }
            })
            .build()

       val result = worker.doWork()
        assertEquals(ListenableWorker.Result.failure(), result)

        coVerify(exactly = 0) { mockUserPreferences.updateLastSyncTime() }
    }
}