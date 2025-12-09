package com.dishut_lampung.sitanihut.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ReportSyncWorkerTest {

    private lateinit var context: Context
    private val apiService: ReportApiService = mockk()
    private val reportDao: ReportDao = mockk()

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun `doWork should return Success when no pending reports`() = runTest {
        coEvery { reportDao.getReportsBySyncStatus("pending_create") } returns emptyList()
        val worker = TestListenableWorkerBuilder<ReportSyncWorker>(context)
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

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }
}