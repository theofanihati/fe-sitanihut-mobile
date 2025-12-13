package com.dishut_lampung.sitanihut.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.SitanihutDatabase
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.local.dao.ReportDao
import com.dishut_lampung.sitanihut.data.local.entity.ReportEntity
import com.dishut_lampung.sitanihut.data.local.entity.SyncStatus
import com.dishut_lampung.sitanihut.data.remote.api.HomeApiService
import com.dishut_lampung.sitanihut.data.remote.response.ApiResponse
import com.dishut_lampung.sitanihut.data.remote.response.PaginatedData
import com.dishut_lampung.sitanihut.data.remote.dto.ReportListItemDto
import com.dishut_lampung.sitanihut.util.Resource
import com.google.gson.Gson
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class HomeRepositoryImplTest {

    private lateinit var repository: HomeRepositoryImpl
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: HomeApiService
    private lateinit var db: SitanihutDatabase
    private lateinit var reportDao: ReportDao

    @MockK
    private lateinit var userPreferences: UserPreferences

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockWebServer = MockWebServer()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HomeApiService::class.java)

        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SitanihutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        reportDao = db.reportDao()
        every { userPreferences.userId } returns flowOf("user-dummy")
        coEvery { userPreferences.getAuthToken() } returns "token-rahasia"

        repository = HomeRepositoryImpl(apiService, reportDao, userPreferences)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
        mockWebServer.shutdown()
        unmockkAll()
    }

    @Test
    fun `getLatestReports should fetch network data immediately and emit the combined result`() = runTest {
        coEvery { userPreferences.getAuthToken() } returns "fake_token"

        val cachedReport = ReportEntity(
            id = "id-local",
            userId = "user-dummy",
            period = 2024,
            month = "agustus",
            date = "2024-08-01",
            nte = 5000.0,
            status = "menunggu",
            syncStatus = SyncStatus.SYNCED
        )
        reportDao.upsertAll(listOf(cachedReport))

        val networkReportDto = ReportListItemDto(
            id = "id-network",
            date = "2024-09-01",
            period = 2024,
            month = "september",
            nte = 8000.0,
            status = "menunggu",
            userId = "user-dummy",
            userName = "Petani Dua"
        )
        val paginatedData = PaginatedData(data = listOf(networkReportDto), totalPages = 1, count = 1)
        val apiResponse = ApiResponse(200, "Success", paginatedData)

        mockWebServer.enqueue(MockResponse().setBody(Gson().toJson(apiResponse)))

        repository.getLatestReports().test {
            val emission = awaitItem()
            assert(emission.isNotEmpty())
            assertEquals("id-network", emission[0].id)

            val localData = emission.find { it.id == "id-local" }
            assert(localData != null)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getLatestReports should limit output to max 10 items even if db has more`() = runTest {
        val manyReports = (1..15).map { i ->
            ReportEntity(
                id = "id-$i",
                userId = "user-dummy",
                period = 2024,
                month = "agustus",
                date = "2024-08-${String.format("%02d", i)}",
                nte = 1000.0,
                status = "menunggu",
                syncStatus = SyncStatus.SYNCED
            )
        }
        reportDao.upsertAll(manyReports)

        coEvery { userPreferences.getAuthToken() } returns "fake_token"

        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        repository.getLatestReports().test {
            val emission = awaitItem()
            assertEquals(10, emission.size)
            assertEquals("id-15", emission[0].id)
            assertEquals("id-6", emission[9].id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getReportsByStatus should fetch network data immediately with Paginated format, save to DB, and emit Success`() = runTest {
        val mockResponseBody = """
            {
                "statusCode": 200,
                "message": "Success",
                "data": {
                    "count": 1,
                    "totalPages": 1,
                    "data": [
                        {
                            "id": "report-api-1",
                            "tanggal": "2024-09-01", 
                            "periode": 2024,
                            "bulan": "september",
                            "nte": 15000.0,
                            "status": "menunggu",
                            "id_user": "user-dummy",
                            "nama_user": "Pak Tani"
                        }
                    ]
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(mockResponseBody).setResponseCode(200))

        repository.getReportsByStatus("menunggu").test {

            val firstItem = awaitItem()
            assertTrue(firstItem is Resource.Loading)

            val secondItem = awaitItem()
            assertTrue(secondItem is Resource.Success)

            val data = (secondItem as Resource.Success).data
            assertEquals(1, data?.size)

            val item = data?.first()
            assertEquals("report-api-1", item?.id)
            assertEquals(15000.0, item?.totalTransaction ?: 0.0, 0.0)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getReportsByStatus should emit local data even if API fails`() = runTest {
        val cachedReport = ReportEntity(
            id = "id-lokal-lama",
            userId = "user-dummy",
            period = 2024,
            month = "agustus",
            date = "2024-08-01",
            nte = 5000.0,
            status = "menunggu",
            syncStatus = SyncStatus.SYNCED
        )
        reportDao.upsertAll(listOf(cachedReport))
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        repository.getReportsByStatus("menunggu").test {

            val firstItem = awaitItem()
            assertTrue(firstItem is Resource.Loading)

            val secondItem = awaitItem()
            assertTrue(secondItem is Resource.Success)

            val data = (secondItem as Resource.Success).data
            assertEquals(1, data?.size)
            assertEquals("id-lokal-lama", data?.first()?.id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getReportsByStatus should handle empty API response gracefully`() = runTest {
        val mockResponseBody = """
            {
                "statusCode": 200,
                "message": "Success",
                "data": {
                    "count": 0,
                    "totalPages": 0,
                    "data": []
                }
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(mockResponseBody).setResponseCode(200))

        repository.getReportsByStatus("menunggu").test {
            awaitItem()

            val successItem = awaitItem()
            assertTrue(successItem is Resource.Success)
            assertTrue(successItem.data?.isEmpty() == true)

            cancelAndIgnoreRemainingEvents()
        }
    }
}