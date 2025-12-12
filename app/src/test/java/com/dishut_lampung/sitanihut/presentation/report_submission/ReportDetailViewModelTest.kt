package com.dishut_lampung.sitanihut.presentation.report_submission

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.ReportDetail
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportDetailUseCase
import com.dishut_lampung.sitanihut.presentation.report_submission.detail.ReportDetailUiState
import com.dishut_lampung.sitanihut.presentation.report_submission.detail.ReportDetailViewModel
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportDetailViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(StandardTestDispatcher())
    private lateinit var getReportDetailUseCase: GetReportDetailUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: ReportDetailViewModel

    private val reportId = "test-id-123"
    private val dummyDetail = ReportDetail(
        id = reportId,
        userName = "Budi",
        userNik = "12345",
        userGender = "Pria",
        userAddress = "Alamat",
        userKphName = "KPH A",
        userKthName = "KTH B",
        month = "Januari",
        period = 2024,
        modal = "1000000",
        farmerNotes = "Catatan",
        penyuluhNotes = "Catatan",
        nte = 500000.0,
        status = ReportStatus.PENDING,
        attachments = emptyList(),
        plantingDetails = emptyList(),
        harvestDetails = emptyList(),
        createdAt = "2024-01-01",
        verifiedAt = null,
        acceptedAt = null
    )

    @Before
    fun setUp() {
        getReportDetailUseCase = mockk()
        savedStateHandle = SavedStateHandle(mapOf("reportId" to reportId))
    }

    @Test
    fun `init should load detail and emit Success state`() = runTest {
        every { getReportDetailUseCase(reportId) } returns flowOf(
            Resource.Loading(),
            Resource.Success(dummyDetail)
        )

        viewModel = ReportDetailViewModel(getReportDetailUseCase, savedStateHandle)
        viewModel.uiState.test {
            val firstItem = awaitItem()
            assertTrue(firstItem is ReportDetailUiState.Loading)

            val secondItem = awaitItem()
            assertTrue(secondItem is ReportDetailUiState.Success)
            assertEquals(dummyDetail, (secondItem as ReportDetailUiState.Success).data)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init should emit Error state when usecase returns Error`() = runTest {
        val errorMessage = "Network Error"
        every { getReportDetailUseCase(reportId) } returns flowOf(
            Resource.Loading(),
            Resource.Error(errorMessage)
        )

        viewModel = ReportDetailViewModel(getReportDetailUseCase, savedStateHandle)
        viewModel.uiState.test {
            assertTrue(awaitItem() is ReportDetailUiState.Loading)

            val errorItem = awaitItem()
            assertTrue(errorItem is ReportDetailUiState.Error)
            assertEquals(errorMessage, (errorItem as ReportDetailUiState.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should NOT emit Loading or Error if state is already Success`() = runTest {
        every { getReportDetailUseCase(reportId) } returns flow {
            emit(Resource.Success(dummyDetail))
            emit(Resource.Loading())
            emit(Resource.Error("New Error"))
        }

        viewModel = ReportDetailViewModel(getReportDetailUseCase, savedStateHandle)

        viewModel.uiState.test {
            assertTrue(awaitItem() is ReportDetailUiState.Loading)

            val successItem = awaitItem()
            assertTrue(successItem is ReportDetailUiState.Success)

            expectNoEvents()
        }
    }
}