package com.dishut_lampung.sitanihut.presentation.penyuluh

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.GetPenyuluhUseCase
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
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
    private lateinit var viewModel: PenyuluhViewModel

    @Before
    fun setUp() {
        getPenyuluhUseCase = mockk()
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

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, userPreferences)
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

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, userPreferences)
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

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, userPreferences)
        advanceUntilIdle()

        viewModel.onEvent(PenyuluhEvent.OnRefresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertFalse(state.isLoading)
        assertEquals(2, state.penyuluhList.size)

        io.mockk.verify(atLeast = 2) { getPenyuluhUseCase("penanggung jawab") }
    }

    @Test
    fun `onEvent OnDismissError should clear error message`() = runTest {
        val errorMessage = "Error terjadi"
        every { getPenyuluhUseCase("penanggung jawab") } returns flowOf(Resource.Error(errorMessage))

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, userPreferences)
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

        viewModel = PenyuluhViewModel(getPenyuluhUseCase, userPreferences)
        advanceUntilIdle()
    }
}