package com.dishut_lampung.sitanihut.data.repository

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.dao.RemoteKeysDao
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.remote.api.ReportApiService
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPagingApi::class)
@RunWith(AndroidJUnit4::class)
class ReportRemoteMediatorTest {

    private lateinit var reportDao: ReportDao
    private lateinit var remoteKeysDao: RemoteKeysDao
    private lateinit var apiService: ReportApiService
    private lateinit var db: SitanihutDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SitanihutDatabase::class.java).build()
        reportDao = db.reportDao()
        remoteKeysDao = db.remoteKeysDao()

        apiService = mockk()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun refreshLoadReturnsSuccessResultWhenMoreDataIsPresent() = runTest {
        val mockApiReports = listOf(
            ReportListItemDto(
                id = "1",
                status = "menunggu",
                date = "20-11-2025",
                period = 2025,
                month = "Januari",
                nte = 23000.0,
                userId = "user-id-123",
                userName = "Petani Cantik 123",
                )
        )
        val mockResponse = ApiResponse(
            statusCode = 200,
            message = "OK",
            data = PaginatedData(
                data = mockApiReports,
                totalPages = 2,
                count = 10
            )
        )

        coEvery {
            apiService.getReports(page = 1, limit = any(), search = any(), status = any())
        } returns mockResponse

        val mediator = ReportRemoteMediator(
            apiService = apiService,
            db = db,
            query = "",
            status = null
        )

        val pagingState = PagingState<Int, ReportEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(10),
            leadingPlaceholderCount = 10
        )

        val result = mediator.load(LoadType.REFRESH, pagingState) // REFRESH = tarik atas

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

        val pagingSource = reportDao.getReports("", null)
        val loadResult = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(loadResult is PagingSource.LoadResult.Page)
        val page = loadResult as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        assertEquals("1", page.data[0].id)
    }
}