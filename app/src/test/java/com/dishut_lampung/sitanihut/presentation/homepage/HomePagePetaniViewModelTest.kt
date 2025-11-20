package com.dishut_lampung.sitanihut.presentation.homepage

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.ReportSummary
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.LogoutUseCase
import com.dishut_lampung.sitanihut.domain.usecase.home.FarmerHomeData
import com.dishut_lampung.sitanihut.domain.usecase.home.GetFarmerHomeDataUseCase
import com.dishut_lampung.sitanihut.presentation.home_page.HomeEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiEvent
import com.dishut_lampung.sitanihut.presentation.home_page.petani.HomePagePetaniViewModel
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomePagePetaniViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var getFarmerHomeDataUseCase: GetFarmerHomeDataUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var homeRepository: HomeRepository
    private lateinit var viewModel: HomePagePetaniViewModel

    private val dummyProfile = UserProfile("Budi Santoso", "Petani", "http://example.com/pic.jpg")
    private val dummySummary = ReportSummary(1,2,3)
    private val dummyReports = listOf(
        Report("id-1", 2025, "Mei", "25-05-2025", 8000.0, ReportStatus.PENDING),
        Report("id-2", 2025, "Mei", "20-05-2025", 5000.0, ReportStatus.REJECTED)
    )
    private val dummyHomeData = FarmerHomeData(dummyProfile, dummySummary, dummyReports)

    @Before
    fun setUp(){
        getFarmerHomeDataUseCase = mockk()
        logoutUseCase = mockk(relaxed = true)
        homeRepository = mockk(relaxed = true)

        every{getFarmerHomeDataUseCase()} returns flowOf(dummyHomeData)

        coEvery {homeRepository.deleteReport(any())} returns Resource.Success(Unit)
        coEvery {homeRepository.submitReport(any())} returns Resource.Success(Unit)
        coEvery { homeRepository.getLatestReports()} returns flowOf(dummyReports)

        viewModel = HomePagePetaniViewModel(
            getFarmerHomeDataUseCase,
            logoutUseCase,
            homeRepository
        )
    }

    @Test
    fun `init should load data successfully and map to correct uiState`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)

        assertEquals(dummyProfile.name, state.userProfile.name)
        assertEquals(dummySummary.pendingCount, state.reportSummary.pendingCount)

        assertEquals(2, state.latestReports.size)

        assertEquals("id-1", state.latestReports[0].id)
        assertEquals("Menunggu", state.latestReports[0].statusDisplay)
        assertTrue(state.latestReports[0].isEditable)
        assertEquals("Laporan Periode Mei 2025", state.latestReports[0].periodTitle)
    }

    @Test
    fun `onRefreshData success should call repository and set isRefreshing to false`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            viewModel.onEvent(HomeEvent.OnRefreshData)

            val refreshState = awaitItem()
            assertTrue(refreshState.isRefreshing)

            val refreshedState = awaitItem()
            assertFalse(refreshedState.isRefreshing)
            assertNull(refreshedState.generalError)

            coVerify(exactly = 1){homeRepository.getLatestReports()}
        }
    }

    @Test
    fun `onLogoutClick should set isLogoutConfirmationVisible to true`() {
        viewModel.onEvent(HomeEvent.OnLogoutClick)
        assertTrue(viewModel.uiState.value.isLogoutConfirmationVisible)
    }

    @Test
    fun `onLogoutConfirm should call LogoutUseCase and emit NavigateToLogin`() = runTest {
        viewModel.eventFlow.test {
            viewModel.onEvent(HomeEvent.OnLogoutConfirm)

            coVerify(exactly = 1) { logoutUseCase() }
            val event = awaitItem()
            assertTrue(event is HomeUiEvent.NavigateToLogin)
        }
    }

    @Test
    fun `onProfileClick should emit NavigateToProfile with Petani role`() = runTest {
        viewModel.eventFlow.test {
            viewModel.onEvent(HomeEvent.OnProfileClick)

            val event = awaitItem()
            assertTrue(event is HomeUiEvent.NavigateToProfile)
            assertEquals(dummyProfile.role, (event as HomeUiEvent.NavigateToProfile).role)
        }
    }

    @Test
    fun `onReportSubmissionMenuClick should emit NavigateToReportSubmission`() = runTest {
        viewModel.eventFlow.test {
            viewModel.onEvent(HomeEvent.OnReportSubmissionMenuClick)
            assertTrue(awaitItem() is HomeUiEvent.NavigateToReportSubmission)
        }
    }

    @Test
    fun `onReportMoreOptionClick should set reportIdForOptionSheet`(){
        val reportId = "raport-id-222"
        viewModel.onEvent(HomeEvent.OnReportMoreOptionClick(reportId))
        assertEquals(reportId, viewModel.uiState.value.reportIdForOptionSheet)
    }

    @Test
    fun `onDeleteClick should close option sheet and show delete confirmation dialog`(){
        val reportId = "raport-id-222"
        viewModel.onEvent(HomeEvent.OnReportMoreOptionClick("apapun"))
        viewModel.onEvent(HomeEvent.OnDeleteClick(reportId))

        assertNull(viewModel.uiState.value.reportIdForOptionSheet)
        assertEquals(reportId, viewModel.uiState.value.reportIdToDelete)
    }

    @Test
    fun `onDeleteConfirm success should call deleteReport and show success message`() = runTest {
        val reportId = "id-to-delete"
        viewModel.uiState.test {
            skipItems(1)
            viewModel.onEvent(HomeEvent.OnDeleteConfirm(reportId))

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.reportIdToDelete)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals("Laporan berhasil dihapus", successState.successMessage)

            coVerify(exactly = 1) { homeRepository.deleteReport(reportId) }
        }
    }

    @Test
    fun `onSubmitClick success should call submitReport and show success message`() = runTest {
        val reportId = "id-to-submit"
        viewModel.uiState.test {
            skipItems(1)
            viewModel.onEvent(HomeEvent.OnSubmitClick(reportId))

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.reportIdForOptionSheet)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals("Laporan berhasil diajukan", successState.successMessage)

            coVerify(exactly = 1) { homeRepository.submitReport(reportId) }
        }
    }

}