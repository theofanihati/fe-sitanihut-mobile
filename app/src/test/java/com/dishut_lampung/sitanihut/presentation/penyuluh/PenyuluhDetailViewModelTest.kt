package com.dishut_lampung.sitanihut.presentation.penyuluh.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.GetPenyuluhDetailUseCase
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PenyuluhDetailViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var getPenyuluhDetailUseCase: GetPenyuluhDetailUseCase
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: PenyuluhDetailViewModel

    private val dummyId = "123"
    private val dummyPenyuluh = Penyuluh(
        id = dummyId,
        name = "Ani Sirani",
        identityNumber = "19900101",
        position = "Penyuluh Ahli",
        gender = "Wanita",
        kphId = "kph-1",
        kphName = "UPTD KPHK Tahura WAR",
        whatsAppNumber = "08123456789"
    )

    @Before
    fun setUp() {
        getPenyuluhDetailUseCase = mockk()
        savedStateHandle = SavedStateHandle(mapOf("penyuluhId" to dummyId))
    }

    @Test
    fun `init should load detail successfully`() = runTest {
        every { getPenyuluhDetailUseCase(dummyId) } returns flowOf(
            Resource.Loading(),
            Resource.Success(dummyPenyuluh)
        )

        viewModel = PenyuluhDetailViewModel(getPenyuluhDetailUseCase, savedStateHandle)

        viewModel.uiState.test {
            val item = awaitItem()

            assertFalse(item.isLoading)
            assertEquals(dummyPenyuluh, item.penyuluh)
            assertNull(item.error)

            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { getPenyuluhDetailUseCase(dummyId) }
    }

    @Test
    fun `init should handle error correctly`() = runTest {
        val errorMessage = "Terjadi kesalahan jaringan"
        every { getPenyuluhDetailUseCase(dummyId) } returns flowOf(
            Resource.Loading(),
            Resource.Error(errorMessage)
        )

        viewModel = PenyuluhDetailViewModel(getPenyuluhDetailUseCase, savedStateHandle)

        viewModel.uiState.test {
            val item = awaitItem()

            assertFalse(item.isLoading)
            assertEquals(errorMessage, item.error)
            assertNull(item.penyuluh)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRetry should re-trigger use case`() = runTest {
        every { getPenyuluhDetailUseCase(dummyId) } returnsMany listOf(
            flowOf(Resource.Error("Error Awal")),
            flowOf(Resource.Success(dummyPenyuluh))
        )

        viewModel = PenyuluhDetailViewModel(getPenyuluhDetailUseCase, savedStateHandle)
        viewModel.uiState.test {
            val initialErrorState = awaitItem()
            assertEquals("Error Awal", initialErrorState.error)
            viewModel.onRetry()

            var nextItem = awaitItem()
            if (nextItem.isLoading) {
                nextItem = awaitItem()
            }

            assertFalse(nextItem.isLoading)
            assertEquals(dummyPenyuluh, nextItem.penyuluh)
            assertNull(nextItem.error)

            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 2) { getPenyuluhDetailUseCase(dummyId) }
    }
}