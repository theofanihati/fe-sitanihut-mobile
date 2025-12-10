package com.dishut_lampung.sitanihut.presentation.pengajuan_laporan

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.domain.usecase.commodity.GetCommoditiesUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.CreateReportUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.ValidateReportInputUseCase
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import com.dishut_lampung.sitanihut.domain.validator.ValidationResult
import com.dishut_lampung.sitanihut.presentation.pengajuan_laporan.create.AddReportEvent
import com.dishut_lampung.sitanihut.presentation.pengajuan_laporan.create.AddReportViewModel
import com.dishut_lampung.sitanihut.presentation.pengajuan_laporan.create.HarvestDetailUiState
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddReportViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    private val getCommoditiesUseCase: GetCommoditiesUseCase = mockk()
    private val createReportUseCase: CreateReportUseCase = mockk()
    private val validateReportInputUseCase: ValidateReportInputUseCase = mockk()

    private lateinit var viewModel: AddReportViewModel

    @Before
    fun setUp() {
        every { getCommoditiesUseCase(any()) } returns flowOf(Resource.Success(emptyList()))

        viewModel = AddReportViewModel(
            getCommoditiesUseCase,
            createReportUseCase,
            validateReportInputUseCase
        )
    }

    @Test
    fun `init should load commodities, generate years, and add default planting item`() = runTest {
        val commodities = listOf(Commodity("1", "jg-01", "Jagung", "buah buahan"))
        every { getCommoditiesUseCase("") } returns flowOf(Resource.Success(commodities))
        viewModel = AddReportViewModel(getCommoditiesUseCase, createReportUseCase, validateReportInputUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(commodities, state.commodityList)
            assertEquals(5, state.periodList.size)
            assertEquals(1, state.plantingDetails.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnModalChange should update modal state`() = runTest {
        viewModel.onEvent(AddReportEvent.OnModalChange("1.000.000"))

        assertEquals("1.000.000", viewModel.uiState.value.modal)
    }

    @Test
    fun `OnAddPlantingDetail should add new item to list`() = runTest {
        assertEquals(1, viewModel.uiState.value.plantingDetails.size)

        viewModel.onEvent(AddReportEvent.OnAddPlantingDetail)
        assertEquals(2, viewModel.uiState.value.plantingDetails.size)
    }

    @Test
    fun `OnPlantingItemChange should update item and auto-set unit`() = runTest {
        val initialItem = viewModel.uiState.value.plantingDetails[0]
        val updatedItem = initialItem.copy(plantType = "tahunan", amount = "100")
        viewModel.onEvent(AddReportEvent.OnPlantingItemChange(0, updatedItem))

        val currentItem = viewModel.uiState.value.plantingDetails[0]
        assertEquals("tahunan", currentItem.plantType)
        assertEquals("batang", currentItem.unit)
    }

    @Test
    fun `OnHarvestItemChange should calculate total price per item and total NTE`() = runTest {
        viewModel.onEvent(AddReportEvent.OnAddHarvestDetail)
        viewModel.onEvent(AddReportEvent.OnAddHarvestDetail)

        val item1 = HarvestDetailUiState(unitPrice = "5000", amount = "10")
        viewModel.onEvent(AddReportEvent.OnHarvestItemChange(0, item1))

        val item2 = HarvestDetailUiState(unitPrice = "10000", amount = "5")
        viewModel.onEvent(AddReportEvent.OnHarvestItemChange(1, item2))

        val state = viewModel.uiState.value

        assertEquals(50000.0, state.harvestDetails[0].totalPrice, 0.0)
        assertEquals(50000.0, state.harvestDetails[1].totalPrice, 0.0)
        assertEquals(100000.0, state.nte, 0.0)
    }

    @Test
    fun `OnSubmit should show error if validation fails`() = runTest {
        every { validateReportInputUseCase.execute(any()) } returns ListValidationResult(false, "Form tidak valid")

        viewModel.onEvent(AddReportEvent.OnSubmit(isAjukan = true))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Form tidak valid", viewModel.uiState.value.error)
        coVerify(exactly = 0) { createReportUseCase(any()) }
    }

    @Test
    fun `OnSubmit should call CreateReportUseCase if validation passes`() = runTest {
        every { validateReportInputUseCase.execute(any()) } returns ListValidationResult(true)
        coEvery { createReportUseCase(any()) } returns Resource.Success(Unit)

        viewModel.onEvent(AddReportEvent.OnModalChange("5000"))
        viewModel.onEvent(AddReportEvent.OnSubmit(isAjukan = true))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Laporan berhasil disimpan!", viewModel.uiState.value.successMessage)

        coVerify(exactly = 1) {
            createReportUseCase(match { input ->
                input.modal == "5000" && input.isAjukan
            })
        }
    }
}