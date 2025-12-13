package com.dishut_lampung.sitanihut.presentation.home_page.petani

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.ReportSummary
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.LogoutUseCase
import com.dishut_lampung.sitanihut.domain.usecase.home.FarmerHomeData
import com.dishut_lampung.sitanihut.domain.usecase.home.GetPetaniHomeDataUseCase
import com.dishut_lampung.sitanihut.presentation.home_page.HomeEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiEvent
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
import java.text.NumberFormat
import java.util.Locale

@ExperimentalCoroutinesApi
class HomePagePetaniViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var getPetaniHomeDataUseCase: GetPetaniHomeDataUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var homeRepository: HomeRepository
    private lateinit var viewModel: HomePagePetaniViewModel

    private val dummyProfile = UserProfile("Budi Santoso", "Petani", "http://example.com/pic.jpg")
    private val dummySummary = ReportSummary(1, 2, 2,3)
    private val dummyReports = listOf(
        Report("id-1", 2025, "Mei", "25-05-2025", 8000000.0, ReportStatus.PENDING),
        Report("id-2", 2025, "Mei", "20-05-2025", 5000000.0, ReportStatus.REJECTED)
    )
    private val dummyHomeData = FarmerHomeData(dummyProfile, dummySummary, dummyReports)

    @Before
    fun setUp() {
        getPetaniHomeDataUseCase = mockk()
        logoutUseCase = mockk(relaxed = true)
        homeRepository = mockk(relaxed = true)

        every { getPetaniHomeDataUseCase() } returns flowOf(dummyHomeData)

        coEvery { homeRepository.deleteReport(any()) } returns Resource.Success(Unit)
        coEvery { homeRepository.submitReport(any()) } returns Resource.Success(Unit)
        coEvery { homeRepository.getLatestReports() } returns flowOf(dummyReports)

        viewModel = HomePagePetaniViewModel(
            getPetaniHomeDataUseCase,
            logoutUseCase,
            homeRepository
        )
    }

    @Test
    fun `init should load data successfully and map to correct uiState`() = runTest {
        viewModel.uiState.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)

            assertEquals(dummyProfile.name, state.userProfile.name)
            assertEquals(dummySummary.pendingCount, state.reportSummary.pendingCount)

            assertEquals(2, state.latestReports.size)

            assertEquals("id-1", state.latestReports[0].id)
            assertEquals("Menunggu", state.latestReports[0].statusDisplay)
            assertFalse(state.latestReports[0].isEditable)
            assertEquals("Laporan Periode Mei 2025", state.latestReports[0].periodTitle)

            val actualNte = state.latestReports[0].nteDisplay
            assertTrue("Harus mengandung 'Rp'", actualNte.contains("Rp"))
            val cleanActual = actualNte.replace("\\s".toRegex(), "").replace("\u00A0", "")
            val cleanExpectedNumber = "8.000.000"
            assertTrue(
                "Harus mengandung angka 8.000.000. Aktual (bersih): $cleanActual",
                cleanActual.contains(cleanExpectedNumber)
            )
        }
    }

    @Test
    fun `onRefreshData success should call repository and set isRefreshing to false`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(HomeEvent.OnRefreshData)

            val refreshState = awaitItem()
            assertTrue(refreshState.isRefreshing)

            val refreshedState = awaitItem()
            assertFalse(refreshedState.isRefreshing)
            assertNull(refreshedState.generalError)

            coVerify(exactly = 1) { homeRepository.getLatestReports() }
        }
    }

    @Test
    fun `onLogoutClick should set isLogoutConfirmationVisible to true`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(HomeEvent.OnLogoutClick)

            val updatedState = awaitItem()
            assertTrue(updatedState.isLogoutConfirmationVisible)
        }
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
    fun `onReportMoreOptionClick should set reportIdForOptionSheet`() = runTest {
        val reportId = "raport-id-222"
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(HomeEvent.OnReportMoreOptionClick(reportId))

            val updatedState = awaitItem()
            assertEquals(reportId, updatedState.reportIdForOptionSheet)
        }
    }

    @Test
    fun `onDeleteClick should close option sheet and show delete confirmation dialog`() = runTest {
        val reportId = "raport-id-222"

        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(HomeEvent.OnReportMoreOptionClick("apapun"))
            awaitItem()
            viewModel.onEvent(HomeEvent.OnDeleteClick(reportId))

            val updatedState = awaitItem()
            assertNull(updatedState.reportIdForOptionSheet)
            assertEquals(reportId, updatedState.reportIdToDelete)
        }
    }

    @Test
    fun `onDeleteConfirm success should call deleteReport and show success message`() = runTest {
        val reportId = "id-to-delete"
        coEvery { homeRepository.deleteReport(reportId) } returns Resource.Success(Unit)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(HomeEvent.OnDeleteClick(reportId))
            awaitItem()
            viewModel.onEvent(HomeEvent.OnDeleteConfirm)

            var nextState = awaitItem()
            while (nextState.successMessage == null || nextState.isLoading) {
                nextState = awaitItem()
            }
            assertFalse(nextState.isLoading)
            assertEquals("Laporan berhasil dihapus", nextState.successMessage)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { homeRepository.deleteReport(reportId) }
    }

    @Test
    fun `onSubmitClick success should call submitReport and show success message`() = runTest {
        val reportId = "id-to-submit"
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(HomeEvent.OnSubmitClick(reportId))
            var nextState = awaitItem()
            if (nextState.isLoading) {
                nextState = awaitItem()
            }

            assertFalse(nextState.isLoading)
            assertEquals("Laporan berhasil diajukan", nextState.successMessage)

            coVerify(exactly = 1) { homeRepository.submitReport(reportId) }
        }
    }
}