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
        TODO()
    }

    private fun loadCommodities() {
        TODO()
    }

    fun onEvent(event: AddReportEvent) {
        TODO()
    }

    private fun submitReport(isAjukan: Boolean) {
        TODO()
    }

    private fun mapStateToInput(isAjukan: Boolean): CreateReportInput {
        TODO()
    }
}