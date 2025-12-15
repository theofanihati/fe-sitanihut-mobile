package com.dishut_lampung.sitanihut.data.repository

import androidx.paging.*
import androidx.room.withTransaction
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.dao.RemoteKeysDao
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
class ReportRemoteMediatorTest {

    private lateinit var apiService: ReportApiService
    private lateinit var db: SitanihutDatabase
    private lateinit var reportDao: ReportDao
    private lateinit var remoteKeysDao: RemoteKeysDao
    private lateinit var mediator: ReportRemoteMediator

    @Before
    fun setUp() {
        apiService = mockk()
        db = mockk()
        reportDao = mockk(relaxed = true)
        remoteKeysDao = mockk(relaxed = true)

        every { db.reportDao() } returns reportDao
        every { db.remoteKeysDao() } returns remoteKeysDao
        mockkStatic("androidx.room.RoomDatabaseKt")

        val transactionLambda = slot<suspend () -> Any>()
        coEvery { db.withTransaction(capture(transactionLambda))
        } coAnswers {
            transactionLambda.captured.invoke()
        }

        mediator = ReportRemoteMediator(
            apiService = apiService,
            db = db,
            query = "",
            status = null
        )
    }

    @Test
    fun `refresh load returns Success and endOfPaginationReached is false when more data present`() = runTest {
        val mockData = listOf(
            ReportListItemDto("1", "2024-01-01", 2024, "Jan", 1.0, "menunggu", "u1", "Budi")
        )
        val apiResponse = ApiResponse(
            200, "OK", PaginatedData(count = 20, totalPages = 2, data = mockData)
        )

        coEvery { apiService.getReports(any(), any(), any(), any()) } returns apiResponse

        val pagingState = PagingState<Int, ReportEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(10),
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

        every { db.reportDao() } returns reportDao
        every { db.remoteKeysDao() } returns remoteKeysDao
    }

    @Test
    fun `refresh load returns Success and endOfPaginationReached is true when data empty`() = runTest {
        val apiResponse = ApiResponse<PaginatedData<ReportListItemDto>>(
            statusCode = 200,
            message = "OK",
            data = PaginatedData(
                count = 0,
                totalPages = 0,
                data = emptyList()
            )
        )
        coEvery { apiService.getReports(any(), any(), any(), any()) } returns apiResponse

        val pagingState = PagingState<Int, ReportEntity>(
            pages = listOf(), anchorPosition = null, config = PagingConfig(10), leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `refresh load returns Error when API fails`() = runTest {
        coEvery { apiService.getReports(any(), any(), any(), any()) } throws IOException("No Internet")

        val pagingState = PagingState<Int, ReportEntity>(
            pages = listOf(), anchorPosition = null, config = PagingConfig(10), leadingPlaceholderCount = 0
        )
        val result = mediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }
}