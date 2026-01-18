package com.dishut_lampung.sitanihut.presentation.penyuluh

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.GetPenyuluhUseCase
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.SyncPenyuluhDataUseCase
import com.dishut_lampung.sitanihut.presentation.penyuluh.list.PenyuluhViewModel
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
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

    private lateinit var getPenyuluhUseCase: GetPenyuluhUseCase
    private lateinit var userPreferences: UserPreferences
    private val syncUseCase: SyncPenyuluhDataUseCase = mockk(relaxed = true)
    private var connectivityObserver: ConnectivityObserver = mockk(relaxed = true)
    private lateinit var viewModel: PenyuluhViewModel

    @Before
    fun setUp() {
        getPenyuluhUseCase = mockk(relaxed = true)
        userPreferences = mockk()

        every { userPreferences.userRole } returns flowOf("penanggung jawab")
    }

    @Test
    fun `init should load penyuluh list successfully`() = runTest {
        val dummyList = listOf(
            Penyuluh("1", "Ahmad", "123", "Ahli", "Pria", "kph1", "KPH A"),
            Penyuluh("2", "Budi", "456", "Terampil", "Pria", "kph1", "KPH A")
        )
        every { getPenyuluhUseCase("penanggung jawab") } returns flowOf(Resource.Success(dummyList))

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, syncUseCase, userPreferences,  connectivityObserver)
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

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, syncUseCase, userPreferences,  connectivityObserver)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.penyuluhList.isEmpty())
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `onEvent OnRefresh should refresh data correctly`() = runTest {
        val dummyList = listOf(
            Penyuluh("1", "Ahmad", "123", "Ahli", "Pria", "kph1", "KPH A"),
            Penyuluh("2", "Budi", "456", "Terampil", "Pria", "kph1", "KPH A")
        )
        every { getPenyuluhUseCase("penanggung jawab") } returns flowOf(Resource.Success(dummyList))

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, syncUseCase, userPreferences,  connectivityObserver)
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

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, syncUseCase, userPreferences, connectivityObserver)
        advanceUntilIdle()

        viewModel.onEvent(PenyuluhEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals("Tidak ada koneksi internet", state.error)

        coVerify(exactly = 0) { syncUseCase() }
    }

    @Test
    fun `onEvent OnRefresh should handle sync error`() = runTest {
        val errorMsg = "Gagal sinkronisasi"
        every { connectivityObserver.observe() } returns flowOf(ConnectivityObserver.Status.Available)
        coEvery { syncUseCase() } returns Resource.Error(errorMsg)

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, syncUseCase, userPreferences, connectivityObserver)
        advanceUntilIdle()

        viewModel.onEvent(PenyuluhEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(errorMsg, state.error)
    }

    @Test
    fun `onEvent OnDismissError should clear error message`() = runTest {
        val errorMessage = "Error terjadi"
        every { getPenyuluhUseCase("penanggung jawab") } returns flowOf(Resource.Error(errorMessage))

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, syncUseCase, userPreferences,  connectivityObserver)
        advanceUntilIdle()
        assertEquals(errorMessage, viewModel.uiState.value.error)

        viewModel.onEvent(PenyuluhEvent.OnDismissError)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(null, state.error)
    }

    @Test
    fun `loading state should be updated when collecting flow`() = runTest {
        val dummyList = emptyList<Penyuluh>()
        every { getPenyuluhUseCase("penanggung jawab") } returns kotlinx.coroutines.flow.flow {
            emit(Resource.Loading())
            emit(Resource.Success(dummyList))
        }

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, syncUseCase, userPreferences,  connectivityObserver)
        advanceUntilIdle()
    }

    @Test
    fun `onEvent OnSearchQueryChange should update query and fetch data after delay`() = runTest {
        val query = "Ahmad"
        every { getPenyuluhUseCase("penanggung jawab", "") } returns flowOf(Resource.Success(emptyList()))
        every { getPenyuluhUseCase("penanggung jawab", query) } returns flowOf(Resource.Success(emptyList()))

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, syncUseCase, userPreferences, connectivityObserver)
        advanceUntilIdle()

        viewModel.onEvent(PenyuluhEvent.OnSearchQueryChange(query))
        assertEquals(query, viewModel.uiState.value.searchQuery)
        advanceUntilIdle()

        coVerify { getPenyuluhUseCase("penanggung jawab", query) }
    }
}