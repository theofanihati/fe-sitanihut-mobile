package com.dishut_lampung.sitanihut.presentation.penyuluh

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.GetPenyuluhUseCase
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.SyncPenyuluhDataUseCase
import com.dishut_lampung.sitanihut.presentation.penyuluh.list.PenyuluhViewModel
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.PdfService
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PenyuluhViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule()

    private val getPenyuluhUseCase: GetPenyuluhUseCase = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk()
    private val syncUseCase: SyncPenyuluhDataUseCase = mockk(relaxed = true)
    private var connectivityObserver: ConnectivityObserver = mockk(relaxed = true)
    private val pdfService: PdfService = mockk(relaxed = true)
    private lateinit var viewModel: PenyuluhViewModel
    private val dummyPenyuluh = listOf(
        Penyuluh("1", "Ahmad", "123", "Ahli", "Pria", "kph1", "KPH A"),
        Penyuluh("2", "Budi", "456", "Terampil", "Pria", "kph1", "KPH A")
    )

    @Before
    fun setUp() {
        every { userPreferences.userRole } returns flowOf("penanggung jawab")
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { getPenyuluhUseCase(any(), any()) } returns flowOf(Resource.Success(dummyPenyuluh))
    }

    private fun setupViewModel() {
        viewModel = PenyuluhViewModel(getPenyuluhUseCase, syncUseCase, userPreferences,  connectivityObserver, pdfService)
    }

    @Test
    fun `init should load penyuluh list successfully`() = runTest {
        val dummyList = listOf(
            Penyuluh("1", "Ahmad", "123", "Ahli", "Pria", "kph1", "KPH A"),
            Penyuluh("2", "Budi", "456", "Terampil", "Pria", "kph1", "KPH A")
        )
        every { getPenyuluhUseCase("penanggung jawab") } returns flowOf(Resource.Success(dummyList))

        setupViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.penyuluhList.size)
        assertEquals("Ahmad", state.penyuluhList[0].name)
    }

    @Test
    fun `init should handle error state`() = runTest {
        val errorMessage = "Gagal memuat data"
        every { getPenyuluhUseCase("penanggung jawab") } returns flowOf(Resource.Error(errorMessage))

        setupViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.penyuluhList.isEmpty())
        assertEquals(errorMessage, state.errorMessage)
    }

    @Test
    fun `onEvent OnRefresh should refresh data correctly`() = runTest {
        val dummyList = listOf(
            Penyuluh("1", "Ahmad", "123", "Ahli", "Pria", "kph1", "KPH A"),
            Penyuluh("2", "Budi", "456", "Terampil", "Pria", "kph1", "KPH A")
        )
        every { getPenyuluhUseCase("penanggung jawab") } returns flowOf(Resource.Success(dummyList))

        setupViewModel()
        advanceUntilIdle()

        viewModel.onEvent(PenyuluhEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertFalse(state.isLoading)
        assertEquals(2, state.penyuluhList.size)

        coVerify(exactly = 1) { getPenyuluhUseCase("penanggung jawab") }
        coVerify(exactly = 1) { syncUseCase() }
    }

    @Test
    fun `onEvent OnRefresh should show error when offline`() = runTest {
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Lost)

        setupViewModel()
        advanceUntilIdle()

        viewModel.onEvent(PenyuluhEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals("Tidak ada koneksi internet", state.errorMessage)

        coVerify(exactly = 0) { syncUseCase() }
    }

    @Test
    fun `onEvent OnRefresh should handle sync error`() = runTest {
        val errorMsg = "Gagal sinkronisasi"
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { syncUseCase() } returns Resource.Error(errorMsg)

        setupViewModel()
        advanceUntilIdle()

        viewModel.onEvent(PenyuluhEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(errorMsg, state.errorMessage)
    }

    @Test
    fun `onEvent OnDismissError should clear error message`() = runTest {
        val errorMessage = "Error terjadi"
        every { getPenyuluhUseCase("penanggung jawab") } returns flowOf(Resource.Error(errorMessage))

        setupViewModel()
        advanceUntilIdle()
        assertEquals(errorMessage, viewModel.uiState.value.errorMessage)

        viewModel.onEvent(PenyuluhEvent.OnDismissError)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `loading state should be updated when collecting flow`() = runTest {
        val dummyList = emptyList<Penyuluh>()
        every { getPenyuluhUseCase("penanggung jawab") } returns kotlinx.coroutines.flow.flow {
            emit(Resource.Loading())
            emit(Resource.Success(dummyList))
        }

        setupViewModel()
        advanceUntilIdle()
    }

    @Test
    fun `onEvent OnSearchQueryChange should update query and fetch data after delay`() = runTest {
        val query = "Ahmad"
        every { getPenyuluhUseCase("penanggung jawab", "") } returns flowOf(Resource.Success(emptyList()))
        every { getPenyuluhUseCase("penanggung jawab", query) } returns flowOf(Resource.Success(emptyList()))

        setupViewModel()
        advanceUntilIdle()

        viewModel.onEvent(PenyuluhEvent.OnSearchQueryChange(query))
        assertEquals(query, viewModel.uiState.value.searchQuery)
        advanceUntilIdle()

        coVerify { getPenyuluhUseCase("penanggung jawab", query) }
    }

    @Test
    fun `exportDataToPdf should update state to success when successful`() = runTest {
        setupViewModel()
        advanceUntilIdle()
        val successMsg = "PDF Saved to Downloads"

        coEvery {
            pdfService.generatePdf(
                fileName = any(),
                reportTitle = any(),
                headers = any(),
                data = any<List<Penyuluh>>(),
                rowMapper = any()
            )
        } returns Resource.Success(successMsg)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(PenyuluhEvent.OnExportList)

            val successItem = expectMostRecentItem()
            assertFalse("Harusnya sudah tidak loading", successItem.isLoading)
            assertEquals(successMsg, successItem.successMessage)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { pdfService.generatePdf<Penyuluh>(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `exportDataToPdf should show error when data is empty`() = runTest {
        coEvery { getPenyuluhUseCase(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)

        setupViewModel()
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(PenyuluhEvent.OnExportList)

            val errorItem = awaitItem()
            assertEquals("Tidak ada data untuk diekspor", errorItem.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { pdfService.generatePdf<Penyuluh>(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `exportDataToPdf should update error state when service fails`() = runTest {
        setupViewModel()
        advanceUntilIdle()
        val errorMsg = "Tidak ada data untuk diekspor"

        coEvery {
            pdfService.generatePdf<Penyuluh>(any(), any(), any(), any(), any())
        } returns Resource.Error(errorMsg)

        viewModel.onEvent(PenyuluhEvent.OnExportList)
        advanceUntilIdle()

        viewModel.uiState.test {
            val finalState = awaitItem()
            assertFalse("Harusnya sudah tidak loading", finalState.isLoading)
            assertEquals(errorMsg, finalState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }
}