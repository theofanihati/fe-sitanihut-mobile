package com.dishut_lampung.sitanihut.presentation.home_page.penyuluh

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.LogoutUseCase
import com.dishut_lampung.sitanihut.presentation.home_page.HomeEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiEvent
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomePagePenyuluhViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @MockK
    private lateinit var homeRepository: HomeRepository

    @MockK
    private lateinit var logoutUseCase: LogoutUseCase

    private lateinit var viewModel: HomePagePenyuluhViewModel

    private val dummyProfile = UserProfile("Penyuluh Budi", "Penyuluh", "url")
    private val reportPending1 = Report("1", 2024, "Agustus", "01-08-2024", 50000.0, ReportStatus.PENDING)
    private val reportPending2 = Report("2", 2024, "Agustus", "02-08-2024", 50000.0, ReportStatus.PENDING)
    private val reportApproved1 = Report("3", 2024, "Juli", "01-07-2024", 50000.0, ReportStatus.APPROVED)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { homeRepository.getUserProfile() } returns flowOf(dummyProfile)
        coEvery { homeRepository.getReportsByStatus("menunggu") } returns flowOf(Resource.Success(emptyList()))
        coEvery { homeRepository.getReportsByStatus("diverifikasi") } returns flowOf(Resource.Success(emptyList()))
        coEvery { homeRepository.getReportsByStatus("disetujui") } returns flowOf(Resource.Success(emptyList()))
        coEvery { homeRepository.getReportsByStatus("ditolak") } returns flowOf(Resource.Success(emptyList()))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `init should load profile and calculate summary correctly`() = runTest {
        val pendingList = listOf(reportPending1, reportPending2)
        val approvedList = listOf(reportApproved1)

        coEvery { homeRepository.getReportsByStatus("menunggu") } returns flowOf(Resource.Success(pendingList))
        coEvery { homeRepository.getReportsByStatus("disetujui") } returns flowOf(Resource.Success(approvedList))

        viewModel = HomePagePenyuluhViewModel(homeRepository, logoutUseCase)
        viewModel.state.test {
            val state = awaitItem()

            assertEquals("Penyuluh Budi", state.userProfile.name)
            assertEquals("Jumlah pending harus 2", 2, state.reportSummary.pendingCount)
            assertEquals("Jumlah approved harus 1", 1, state.reportSummary.approvedCount)

            assertEquals(2, state.latestReports.size)
            assertEquals("1", state.latestReports[0].id)

            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `ReportUiModel should have correct mapping for Penyuluh`() = runTest {
        val pendingList = listOf(reportPending1)
        coEvery { homeRepository.getReportsByStatus("menunggu") } returns flowOf(Resource.Success(pendingList))

        viewModel = HomePagePenyuluhViewModel(homeRepository, logoutUseCase)
        viewModel.state.test {
            val state = awaitItem()
            val item = state.latestReports.first()

            assertEquals("1", item.id)

            assertFalse("Penyuluh should not be able to edit", item.isEditable)
            assertFalse("Penyuluh should not be able to delete", item.isDeletable)
        }
    }

    @Test
    fun `when error occurs in one flow, generalError should be updated`() = runTest {
        coEvery { homeRepository.getReportsByStatus("menunggu") } returns flowOf(Resource.Error("Network Error"))
        coEvery { homeRepository.getReportsByStatus("disetujui") } returns flowOf(Resource.Success(emptyList()))

        viewModel = HomePagePenyuluhViewModel(homeRepository, logoutUseCase)
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Network Error", state.generalError)
            assertTrue(state.latestReports.isEmpty())
        }
    }

    @Test
    fun `onEvent OnRefreshData should trigger reload`() = runTest {
        viewModel = HomePagePenyuluhViewModel(homeRepository, logoutUseCase)

        viewModel.onEvent(HomeEvent.OnRefreshData)
        coVerify(atLeast = 2) { homeRepository.getUserProfile() }
        coVerify(atLeast = 2) { homeRepository.getReportsByStatus("menunggu") }
        coVerify(atLeast = 2) { homeRepository.getReportsByStatus("disetujui") }
    }

    @Test
    fun `onEvent OnLogoutConfirm should call logout usecase and navigate`() = runTest {
        viewModel = HomePagePenyuluhViewModel(homeRepository, logoutUseCase)
        coEvery { logoutUseCase() } returns Unit

        viewModel.eventFlow.test {
            viewModel.onEvent(HomeEvent.OnLogoutConfirm)

            coVerify { logoutUseCase() }
            assertEquals(HomeUiEvent.NavigateToLogin, awaitItem())
        }
    }

    @Test
    fun `onEvent OnViewDetailClick should navigate to report detail`() = runTest {
        viewModel = HomePagePenyuluhViewModel(homeRepository, logoutUseCase)

        viewModel.eventFlow.test {
            viewModel.onEvent(HomeEvent.OnViewDetailClick("123"))

            val event = awaitItem()
            assertTrue(event is HomeUiEvent.NavigateToReportDetail)
            assertEquals("123", (event as HomeUiEvent.NavigateToReportDetail).reportId)
        }
    }
}