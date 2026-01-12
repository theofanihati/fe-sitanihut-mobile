package com.dishut_lampung.sitanihut.presentation.petani

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.usecase.petani.GetPetaniDetailUseCase
import com.dishut_lampung.sitanihut.presentation.petani.detail.PetaniDetailViewModel
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PetaniDetailViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private var getPetaniDetailUseCase: GetPetaniDetailUseCase = mockk()
    private var userPreferences: UserPreferences = mockk()
    private var savedStateHandle: SavedStateHandle = mockk()
    private lateinit var viewModel: PetaniDetailViewModel

    @Before
    fun setUp() {
        every { userPreferences.userRole } returns flowOf("penyuluh")
    }

    @Test
    fun `init should load petani detail successfully`() = runTest {
        val id = "123"
        val dummyPetani = Petani(id, "petani", "1234567890123456", "wanita", "dimana ya",
            "081234567890", "sma", "ART",5.0,"1", "kph", "2", "kth")

        every { savedStateHandle.get<String>("id") } returns id
        every { getPetaniDetailUseCase(id) } returns flowOf(
            Resource.Loading(),
            Resource.Success(dummyPetani)
        )

        viewModel = PetaniDetailViewModel(getPetaniDetailUseCase, userPreferences, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertEquals(dummyPetani, state.petani)
            assertNull(state.error)
            assertEquals("penyuluh", state.userRole)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init should set error state when usecase throws exception`() = runTest {
        val id = "123"
        val errorMessage = "Database Error"

        every { savedStateHandle.get<String>("id") } returns id
        every { getPetaniDetailUseCase(id) } returns flow {
            throw RuntimeException(errorMessage)
        }

        viewModel = PetaniDetailViewModel(getPetaniDetailUseCase, userPreferences, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertEquals(errorMessage, state.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init should handle data not found (null)`() = runTest {
        val id = "999"
        every { savedStateHandle.get<String>("id") } returns id
        every { getPetaniDetailUseCase(id) } returns flowOf(
            Resource.Loading(),
            Resource.Error("Data tidak ditemukan")
        )

        viewModel = PetaniDetailViewModel(getPetaniDetailUseCase, userPreferences, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertNull(state.petani)
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test
    fun `onRetry should trigger load detail again`() = runTest {
        val id = "123"
        val dummyKth = Petani(id, "Petani", "", "", "", "", "", "", 0.00, "", "", "", "")

        every { savedStateHandle.get<String>("id") } returns id
        var callCount = 0
        every { getPetaniDetailUseCase(id) } answers {
            callCount++
            if (callCount == 1) {
                flowOf(
                    Resource.Loading(),
                    Resource.Error("Error Pertama")
                )
            } else {
                flowOf(
                    Resource.Loading(),
                    Resource.Success(dummyKth)
                )
            }
        }

        viewModel = PetaniDetailViewModel(getPetaniDetailUseCase, userPreferences, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) state = awaitItem()
            assertEquals("Error Pertama", state.error)
            assertNull(state.petani)
        }

        viewModel.onRetry()
        viewModel.uiState.test {
            var state = awaitItem()
            if (!state.isLoading) {
                if (state.error != null) state = awaitItem()
            }

            while (state.isLoading) state = awaitItem()
            assertEquals(dummyKth, state.petani)
            assertNull(state.error)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 2) { getPetaniDetailUseCase(id) }
    }

    @Test
    fun `init should set default error message when exception message is null`() = runTest {
        val id = "123"

        every { savedStateHandle.get<String>("id") } returns id
        every { getPetaniDetailUseCase(id) } returns flow { throw RuntimeException() }

        viewModel = PetaniDetailViewModel(getPetaniDetailUseCase, userPreferences, savedStateHandle)

        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) state = awaitItem()

            assertFalse(state.isLoading)
            assertEquals("Terjadi kesalahan", state.error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}