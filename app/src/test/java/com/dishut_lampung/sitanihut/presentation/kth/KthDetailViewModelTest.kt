package com.dishut_lampung.sitanihut.presentation.kth

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthDetailUseCase
import com.dishut_lampung.sitanihut.presentation.kth.detail.KthDetailViewModel
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KthDetailViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var getKthDetailUseCase: GetKthDetailUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: KthDetailViewModel

    @Before
    fun setUp() {
        getKthDetailUseCase = mockk()
        savedStateHandle = mockk()
    }

    @Test
    fun `init should load KTH detail successfully`() = runTest {
        val id = "123"
        val dummyKth = Kth(id, "KTH Mawar", "Desa A", "Kec B", "Kab C", "Ketua A", "081", "KPH X")

        every { savedStateHandle.get<String>("kthId") } returns id
        every { getKthDetailUseCase(id) } returns flowOf(dummyKth)

        viewModel = KthDetailViewModel(getKthDetailUseCase, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertEquals(dummyKth, state.kth)
            assertNull(state.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init should set error state when usecase throws exception`() = runTest {
        val id = "123"
        val errorMessage = "Database Error"

        every { savedStateHandle.get<String>("kthId") } returns id
        every { getKthDetailUseCase(id) } returns flow {
            throw RuntimeException(errorMessage)
        }

        viewModel = KthDetailViewModel(getKthDetailUseCase, savedStateHandle)
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
        every { savedStateHandle.get<String>("kthId") } returns id
        every { getKthDetailUseCase(id) } returns flowOf(null)

        viewModel = KthDetailViewModel(getKthDetailUseCase, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertNull(state.kth)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test
    fun `onRetry should trigger load detail again`() = runTest {
        val id = "123"
        val dummyKth = Kth(id, "KTH Mawar", "", "", "", "", "", "")

        every { savedStateHandle.get<String>("kthId") } returns id
        var callCount = 0
        every { getKthDetailUseCase(id) } answers {
            callCount++
            if (callCount == 1) {
                flow { throw RuntimeException("Error Pertama") }
            } else {
                flowOf(dummyKth)
            }
        }

        viewModel = KthDetailViewModel(getKthDetailUseCase, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) state = awaitItem()
            assertEquals("Error Pertama", state.error)
            assertNull(state.kth)
        }

        viewModel.onRetry()
        viewModel.uiState.test {
            var state = awaitItem()
            if (!state.isLoading) {
                if (state.error != null) state = awaitItem()
            }

            while (state.isLoading) state = awaitItem()
            assertEquals(dummyKth, state.kth)
            assertNull(state.error)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 2) { getKthDetailUseCase(id) }
    }

    @Test
    fun `init should set default error message when exception message is null`() = runTest {
        val id = "123"

        every { savedStateHandle.get<String>("kthId") } returns id
        every { getKthDetailUseCase(id) } returns flow { throw RuntimeException() }

        viewModel = KthDetailViewModel(getKthDetailUseCase, savedStateHandle)

        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) state = awaitItem()

            assertFalse(state.isLoading)
            assertEquals("Terjadi kesalahan", state.error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}