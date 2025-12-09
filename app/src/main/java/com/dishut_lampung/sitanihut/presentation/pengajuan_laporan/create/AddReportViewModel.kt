package com.dishut_lampung.sitanihut.presentation.pengajuan_laporan.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.model.CreateReportInput
import com.dishut_lampung.sitanihut.domain.model.MasaPanen
import com.dishut_lampung.sitanihut.domain.model.MasaTanam
import com.dishut_lampung.sitanihut.domain.usecase.commodity.GetCommoditiesUseCase // Reuse yg ada
import com.dishut_lampung.sitanihut.domain.usecase.report.CreateReportUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.ValidateReportInputUseCase
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddReportViewModel @Inject constructor(
    private val getCommoditiesUseCase: GetCommoditiesUseCase,
    private val createReportUseCase: CreateReportUseCase,
    private val validateReportInputUseCase: ValidateReportInputUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReportState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCommodities()
        onEvent(AddReportEvent.OnAddPlantingDetail)
        generatePeriodList()
    }

    private fun generatePeriodList() {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val years = (0..4).map { (currentYear - it).toString() }
        _uiState.update { it.copy(periodList = years) }
    }

    private fun loadCommodities() {
        getCommoditiesUseCase("").onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(commodityList = result.data ?: emptyList()) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = "Gagal memuat komoditas") }
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: AddReportEvent) {
        when (event) {
            is AddReportEvent.OnMonthChange -> _uiState.update { it.copy(month = event.month) }
            is AddReportEvent.OnPeriodChange -> _uiState.update { it.copy(period = event.period) }
            is AddReportEvent.OnModalChange -> _uiState.update { it.copy(modal = event.value) }
            is AddReportEvent.OnFarmerNotesChange -> _uiState.update { it.copy(farmerNotes = event.value) }

            AddReportEvent.OnAddPlantingDetail -> {
                val newList = _uiState.value.plantingDetails + PlantingDetailUiState()
                _uiState.update { it.copy(plantingDetails = newList) }
            }
            is AddReportEvent.OnRemovePlantingDetail -> {
                val newList = _uiState.value.plantingDetails.toMutableList().apply { removeAt(event.index) }
                _uiState.update { it.copy(plantingDetails = newList) }
            }
            is AddReportEvent.OnPlantingItemChange -> {
                val newList = _uiState.value.plantingDetails.toMutableList()
                newList[event.index] = event.item

                if (event.item.plantType == "tahunan") newList[event.index] = newList[event.index].copy(unit = "batang")
                if (event.item.plantType == "semusim") newList[event.index] = newList[event.index].copy(unit = "kg")

                _uiState.update { it.copy(plantingDetails = newList) }
            }

            AddReportEvent.OnAddHarvestDetail -> {
                val newList = _uiState.value.harvestDetails + HarvestDetailUiState()
                _uiState.update { it.copy(harvestDetails = newList) }
            }
            is AddReportEvent.OnRemoveHarvestDetail -> {
                val newHarvestList = _uiState.value.harvestDetails.toMutableList().apply { removeAt(event.index) }
                _uiState.update { it.copy(harvestDetails = newHarvestList) }
            }
            is AddReportEvent.OnHarvestItemChange -> {
                val newList = _uiState.value.harvestDetails.toMutableList()

                //total Price (Harga * Jumlah)
                val price = event.item.unitPrice.toDoubleOrNull() ?: 0.0
                val amount = event.item.amount.toIntOrNull() ?: 0
                val updatedItem = event.item.copy(totalPrice = price * amount)

                newList[event.index] = updatedItem

                //total NTE (Sum semua total price)
                val newNte = newList.sumOf { it.totalPrice }

                _uiState.update { it.copy(harvestDetails = newList, nte = newNte) }
            }

            is AddReportEvent.OnAddAttachment -> {
                val currentFiles = _uiState.value.attachments
                val newFiles = currentFiles + event.file
                _uiState.update { it.copy(attachments = newFiles) }
            }
            is AddReportEvent.OnRemoveAttachment -> {
                val currentFiles = _uiState.value.attachments.toMutableList()
                if (event.index in currentFiles.indices) {
                    currentFiles.removeAt(event.index)
                    _uiState.update { it.copy(attachments = currentFiles) }
                }
            }

            is AddReportEvent.OnSubmit -> submitReport(event.isAjukan)
            AddReportEvent.OnDismissMessage -> _uiState.update { it.copy(error = null, successMessage = null) }

            else -> {}
        }
    }

    private fun submitReport(isAjukan: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val input = mapStateToInput(isAjukan)
            val validationResult = validateReportInputUseCase.execute(input)

            if (!validationResult.successful) {
                _uiState.update { it.copy(isLoading = false, error = validationResult.errorMessage) }
                return@launch
            }

            when (val result = createReportUseCase(input)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Laporan berhasil disimpan!") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    private fun mapStateToInput(isAjukan: Boolean): CreateReportInput {
        val s = _uiState.value
        return CreateReportInput(
            month = s.month,
            period = s.period.toIntOrNull() ?: 0,
            modal = s.modal,
            farmerNotes = s.farmerNotes,
            nte = s.nte,
            isAjukan = isAjukan,
            attachments = s.attachments,
            plantingDetails = s.plantingDetails.map {
                MasaTanam(it.commodityId, it.plantType, it.plantDate, it.plantAge.toDoubleOrNull() ?: 0.0, it.amount)
            },
            harvestDetails = s.harvestDetails.map {
                MasaPanen(it.harvestDate, it.commodityId, it.unitPrice, it.amount)
            }
        )
    }
}