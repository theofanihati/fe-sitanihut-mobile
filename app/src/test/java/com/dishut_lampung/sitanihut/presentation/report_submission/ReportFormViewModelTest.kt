package com.dishut_lampung.sitanihut.presentation.report_submission

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.Commodity
import com.dishut_lampung.sitanihut.domain.model.ReportDetail
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.usecase.commodity.GetCommoditiesUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.CreateReportUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.UpdateReportUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.ValidateReportInputUseCase
import com.dishut_lampung.sitanihut.domain.validator.ListValidationResult
import com.dishut_lampung.sitanihut.presentation.report_submission.form.ReportFormViewModel
import com.dishut_lampung.sitanihut.presentation.report_submission.form.HarvestDetailUiState
import com.dishut_lampung.sitanihut.presentation.report_submission.form.ReportFormEvent
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
class ReportFormViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    private val getCommoditiesUseCase: GetCommoditiesUseCase = mockk()
    private val createReportUseCase: CreateReportUseCase = mockk()
    private val validateReportInputUseCase: ValidateReportInputUseCase = mockk()
    private val getReportDetailUseCase: GetReportDetailUseCase = mockk()
    private val updateReportUseCase: UpdateReportUseCase = mockk()
    private val savedStateHandle: SavedStateHandle = mockk()

    @Before
    fun setUp() {
        every { getCommoditiesUseCase(any()) } returns flowOf(Resource.Success(emptyList()))
        every { savedStateHandle.get<String>("reportId") } returns null
    }

    private fun createViewModel(
        customSavedState: SavedStateHandle? = null
    ): ReportFormViewModel {
        val stateHandle = customSavedState ?: savedStateHandle

        return ReportFormViewModel(
            getCommoditiesUseCase,
            createReportUseCase,
            validateReportInputUseCase,
            getReportDetailUseCase,
            updateReportUseCase,
            stateHandle
        )
    }

    @Test
    fun `init should load commodities, generate years, and add default planting item`() = runTest {
        val commodities = listOf(Commodity("1", "jg-01", "Jagung", "buah buahan"))
        every { getCommoditiesUseCase("") } returns flowOf(Resource.Success(commodities))

        var viewModel = createViewModel()
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
        var viewModel = createViewModel()
        viewModel.onEvent(ReportFormEvent.OnModalChange("1.000.000"))
        assertEquals("1.000.000", viewModel.uiState.value.modal)
    }

    @Test
    fun `OnAddPlantingDetail should add new item to list`() = runTest {
        var viewModel = createViewModel()
        assertEquals(1, viewModel.uiState.value.plantingDetails.size)

        viewModel.onEvent(ReportFormEvent.OnAddPlantingDetail)
        assertEquals(2, viewModel.uiState.value.plantingDetails.size)
    }

    @Test
    fun `OnRemovePlantingDetail should remove item at specific index`() = runTest {
        var viewModel = createViewModel()

        viewModel.onEvent(ReportFormEvent.OnAddPlantingDetail)
        viewModel.onEvent(ReportFormEvent.OnAddPlantingDetail)
        assertEquals(3, viewModel.uiState.value.plantingDetails.size)

        viewModel.onEvent(ReportFormEvent.OnRemovePlantingDetail(1))
        assertEquals(2, viewModel.uiState.value.plantingDetails.size)
    }

    @Test
    fun `OnPlantingItemChange should update item and auto-set unit`() = runTest {
        var viewModel = createViewModel()
        val initialItem = viewModel.uiState.value.plantingDetails[0]
        val updatedItem = initialItem.copy(plantType = "tahunan", amount = "100")
        viewModel.onEvent(ReportFormEvent.OnPlantingItemChange(0, updatedItem))

        val currentItem = viewModel.uiState.value.plantingDetails[0]
        assertEquals("tahunan", currentItem.plantType)
        assertEquals("batang", currentItem.unit)
    }

    @Test
    fun `OnPlantingItemChange with type Semusim and Date should calculate plant age`() = runTest {
        var viewModel = createViewModel()
        val dateString = "01/01/2020"
        val item = com.dishut_lampung.sitanihut.presentation.report_submission.form.PlantingDetailUiState(
            plantType = "semusim",
            plantDate = dateString,
            amount = "10"
        )

        viewModel.onEvent(ReportFormEvent.OnPlantingItemChange(0, item))

        val updatedItem = viewModel.uiState.value.plantingDetails[0]
        val age = updatedItem.plantAge.toDoubleOrNull() ?: 0.0

        assertEquals("kg", updatedItem.unit)
        assert(age > 1.0)
    }

    @Test
    fun `OnAddHarvestDetail should add new item to list`() = runTest {
        var viewModel = createViewModel()
        assertEquals(1, viewModel.uiState.value.harvestDetails.size)

        viewModel.onEvent(ReportFormEvent.OnAddHarvestDetail)
        assertEquals(2, viewModel.uiState.value.harvestDetails.size)
    }

    @Test
    fun `OnHarvestItemChange should calculate total price per item and total NTE`() = runTest {
        var viewModel = createViewModel()
        viewModel.onEvent(ReportFormEvent.OnAddHarvestDetail)
        viewModel.onEvent(ReportFormEvent.OnAddHarvestDetail)

        val item1 = HarvestDetailUiState(unitPrice = "5000", amount = "10")
        viewModel.onEvent(ReportFormEvent.OnHarvestItemChange(0, item1))

        val item2 = HarvestDetailUiState(unitPrice = "10000", amount = "5")
        viewModel.onEvent(ReportFormEvent.OnHarvestItemChange(1, item2))

        val state = viewModel.uiState.value

        assertEquals(50000.0, state.harvestDetails[0].totalPrice, 0.0)
        assertEquals(50000.0, state.harvestDetails[1].totalPrice, 0.0)
        assertEquals(100000.0, state.nte, 0.0)
    }

    @Test
    fun `OnRemoveHarvestDetail should remove item and recalculate NTE`() = runTest {
        var viewModel = createViewModel()
        viewModel.onEvent(ReportFormEvent.OnAddHarvestDetail)
        viewModel.onEvent(ReportFormEvent.OnAddHarvestDetail)

        viewModel.onEvent(ReportFormEvent.OnHarvestItemChange(0,
            HarvestDetailUiState(unitPrice = "5000", amount = "10")
        ))
        viewModel.onEvent(ReportFormEvent.OnHarvestItemChange(1,
            HarvestDetailUiState(unitPrice = "2000", amount = "10")
        ))

        assertEquals(70000.0, viewModel.uiState.value.nte, 0.0)

        viewModel.onEvent(ReportFormEvent.OnRemoveHarvestDetail(0))
        assertEquals(2, viewModel.uiState.value.harvestDetails.size)
        assertEquals(20000.0, viewModel.uiState.value.nte, 0.0)
    }

    @Test
    fun `OnSubmit should fail validation and map errors to specific fields`() = runTest {
        var viewModel = createViewModel()
        val fieldErrors = mapOf(
            "modal" to "Modal harus angka",
            "plant_amount_0" to "Jumlah wajib diisi"
        )

        every { validateReportInputUseCase.execute(any()) } returns
                ListValidationResult(successful = false, errorMessage = "Cek Input", fieldErrors = fieldErrors)

        viewModel.onEvent(ReportFormEvent.OnSubmit(true))
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals("Cek Input", state.error)
        coVerify(exactly = 0) { createReportUseCase(any()) }

        assertEquals("Modal harus angka", state.modalError)
        assertEquals("Jumlah wajib diisi", state.plantingDetails[0].amountError)
    }

    @Test
    fun `OnSubmit in Create Mode should call CreateReportUseCase if validation passes`() = runTest {
        every { validateReportInputUseCase.execute(any()) } returns ListValidationResult(true)
        coEvery { createReportUseCase(any()) } returns Resource.Success(Unit)

        var viewModel = createViewModel()
        viewModel.onEvent(ReportFormEvent.OnModalChange("5000"))
        viewModel.onEvent(ReportFormEvent.OnSubmit(isAjukan = true))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Berhasil disimpan!", viewModel.uiState.value.successMessage)

        coVerify(exactly = 1) {
            createReportUseCase(match { input ->
                input.modal == "5000" && input.isAjukan
            })
        }
    }

    @Test
    fun `init in Edit Mode with reportId should load existing report data`() = runTest {
        val reportId = "report-123"
        val editStateHandle = SavedStateHandle(mapOf("reportId" to reportId))

        val dummy = ReportDetail(
            id = reportId,
            modal = "999",
            month = "Januari",
            period = 2024,
            farmerNotes = "Catatan",
            penyuluhNotes = "Catatan",
            nte = 0.0,
            status = ReportStatus.DRAFT,
            attachments = emptyList(),
            plantingDetails = emptyList(),
            harvestDetails = emptyList()
        )

        every { getReportDetailUseCase(reportId) } returns flowOf(Resource.Success(dummy))

        val viewModel = createViewModel(customSavedState = editStateHandle)
        advanceUntilIdle()
        coVerify { getReportDetailUseCase(reportId) }
        assertEquals("999", viewModel.uiState.value.modal)
    }

    @Test
    fun `OnSubmit in Edit Mode should call UpdateReportUseCase`() = runTest {
        val reportId = "report-123"
        val editStateHandle = SavedStateHandle(mapOf("reportId" to reportId))
        every { getReportDetailUseCase(reportId) } returns flowOf(Resource.Success(mockk(relaxed = true)))
        every { validateReportInputUseCase.execute(any()) } returns ListValidationResult(true)
        coEvery { updateReportUseCase(any(), any()) } returns Resource.Success(true)

        val viewModel = createViewModel(customSavedState = editStateHandle)
        advanceUntilIdle()

        viewModel.onEvent(ReportFormEvent.OnSubmit(true))
        advanceUntilIdle()

        coVerify(exactly = 1) { updateReportUseCase(eq(reportId), any()) }
        assertEquals("Perubahan disimpan!", viewModel.uiState.value.successMessage)
    }
}