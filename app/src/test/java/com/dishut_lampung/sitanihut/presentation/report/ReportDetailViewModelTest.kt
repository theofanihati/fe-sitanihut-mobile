package com.dishut_lampung.sitanihut.presentation.report

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.ReportDetail
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.ReviewReportUseCase
import com.dishut_lampung.sitanihut.presentation.report.detail.ReportDetailEvent
import com.dishut_lampung.sitanihut.presentation.report.detail.ReportDetailUiState
import com.dishut_lampung.sitanihut.presentation.report.detail.ReportDetailViewModel
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportDetailViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule(StandardTestDispatcher())
    private lateinit var getReportDetailUseCase: GetReportDetailUseCase
    private lateinit var reviewReportUseCase: ReviewReportUseCase
    private lateinit var userPreferences: UserPreferences
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
        reviewReportUseCase = mockk()
        userPreferences = mockk()
        savedStateHandle = SavedStateHandle(mapOf("reportId" to reportId))

        every { userPreferences.userRole } returns flowOf("petani")
        coEvery { reviewReportUseCase(any(), any(), any()) } returns Resource.Success(Unit)
    }

    @Test
    fun `init should load detail and show correct buttons for Penyuluh`() = runTest {
        val role = "penyuluh"
        io.mockk.clearMocks(userPreferences)
        every { userPreferences.userRole } returns flowOf(role)
        every { getReportDetailUseCase(reportId) } returns flowOf(
            Resource.Loading(),
            Resource.Success(dummyDetail)
        )

        viewModel = ReportDetailViewModel(getReportDetailUseCase, reviewReportUseCase, userPreferences, savedStateHandle)
        viewModel.uiState.test {
            val firstItem = awaitItem()
            assertTrue(firstItem is ReportDetailUiState.Loading)

            val secondItem = awaitItem() as ReportDetailUiState.Success
            assertTrue("Harus bisa verifikasi", secondItem.canVerify)
            assertTrue("Harus bisa tolak", secondItem.canReject)
            assertFalse("Belum bisa setujui", secondItem.canApprove)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init should load detail and show correct buttons for PJ`() = runTest {
        val role = "pj"
        val verifiedReport = dummyDetail.copy(status = ReportStatus.VERIFIED)

        io.mockk.clearMocks(userPreferences)
        every { userPreferences.userRole } returns flowOf(role)
        every { getReportDetailUseCase(reportId) } returns flowOf(Resource.Success(verifiedReport))

        viewModel = ReportDetailViewModel(getReportDetailUseCase, reviewReportUseCase, userPreferences, savedStateHandle)

        viewModel.uiState.test {
            val firstItem = awaitItem()
            assertTrue(firstItem is ReportDetailUiState.Loading)

            val secondItem = awaitItem() as ReportDetailUiState.Success
            assertTrue("Harus bisa setujui", secondItem.canApprove)
            assertTrue("Harus bisa tolak", secondItem.canReject)
            assertFalse("Tidak perlu verifikasi lagi", secondItem.canVerify)

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

        viewModel = ReportDetailViewModel(getReportDetailUseCase, reviewReportUseCase, userPreferences, savedStateHandle)
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

        viewModel = ReportDetailViewModel(getReportDetailUseCase, reviewReportUseCase, userPreferences, savedStateHandle)

        viewModel.uiState.test {
            assertTrue(awaitItem() is ReportDetailUiState.Loading)
            assertTrue(awaitItem() is ReportDetailUiState.Success)
            assertTrue(awaitItem() is ReportDetailUiState.Loading)
            expectNoEvents()
        }
    }

    @Test
    fun `onVerifyClick should call reviewUseCase with verified status`() = runTest {
        val role = "penyuluh"
        io.mockk.clearMocks(userPreferences)
        every { userPreferences.userRole } returns flowOf(role)
        every { getReportDetailUseCase(reportId) } returns flowOf(Resource.Success(dummyDetail))

        viewModel = ReportDetailViewModel(getReportDetailUseCase, reviewReportUseCase, userPreferences, savedStateHandle)

        viewModel.uiState.test {
            awaitItem(); awaitItem()
            viewModel.onEvent(ReportDetailEvent.OnVerifyClick)

            val loadingState = awaitItem() as ReportDetailUiState.Success
            assertTrue(loadingState.isActionLoading)

            val successState = awaitItem() as ReportDetailUiState.Success
            assertFalse(successState.isActionLoading)
            assertEquals("Laporan berhasil diverifikasi", successState.actionMessage)

            coVerify { reviewReportUseCase(reportId, ReportStatus.VERIFIED, role) }
        }
    }
}