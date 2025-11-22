package com.dishut_lampung.sitanihut.presentation.homepage

import com.dishut_lampung.sitanihut.domain.usecase.information.DownloadStructureImageUseCase
import com.dishut_lampung.sitanihut.domain.validator.ValidationResult
import com.dishut_lampung.sitanihut.presentation.information.InformationEvent
import com.dishut_lampung.sitanihut.presentation.information.about_company.DishutViewModel
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DishutViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var downloadUseCase: DownloadStructureImageUseCase
    private lateinit var viewModel: DishutViewModel

    @Before
    fun setUp() {
        downloadUseCase = mockk(relaxed = true)
        viewModel = DishutViewModel(downloadUseCase)
    }

    @Test
    fun `onDownloadClick should update state with successMessage when use case returns success`() = runTest {
        val dummySuccessMsg = "Gambar berhasil disimpan"
        coEvery { downloadUseCase() } returns Result.success(dummySuccessMsg)
        viewModel.onEvent(InformationEvent.onDownloadClick)

        val currentState = viewModel.state.value
        assertFalse(currentState.isLoading)
        assertNull(currentState.generalError)
        assertEquals(dummySuccessMsg, currentState.successMessage)
    }

    @Test
    fun `onDownloadClick should update state with generalError when use case returns failure`() = runTest {
        val dummyErrorMsg = "Gagal menyimpan gambar"
        coEvery { downloadUseCase() } returns Result.failure(Exception(dummyErrorMsg))

        viewModel.onEvent(InformationEvent.onDownloadClick)
        val currentState = viewModel.state.value
        assertFalse(currentState.isLoading)
        assertNull(currentState.successMessage)

        assertEquals(dummyErrorMsg, currentState.generalError)
    }
}