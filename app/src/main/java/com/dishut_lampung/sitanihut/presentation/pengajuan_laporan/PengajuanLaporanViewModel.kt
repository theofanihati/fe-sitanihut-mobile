package com.dishut_lampung.sitanihut.presentation.pengajuan_laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.dishut_lampung.sitanihut.domain.usecase.report.DeleteReportUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportsUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.SubmitReportUseCase
import com.dishut_lampung.sitanihut.presentation.home_page.petani.toUiModel
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PengajuanLaporanViewModel @Inject constructor(
    private val getReportsUseCase: GetReportsUseCase,
    private val deleteReportUseCase: DeleteReportUseCase,
    private val submitReportUseCase: SubmitReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PengajuanLaporanUiState())
    val uiState = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val reportPagingFlow = _uiState
        .map { it.searchQuery to it.selectedStatus }
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { (query, status) ->
            getReportsUseCase(query, status)
                .map { pagingData ->
                    pagingData.map { report -> report.toUiModel() }
                }
                .cachedIn(viewModelScope)
        }

    fun onEvent(event: PengajuanLaporanEvent) {
        when (event) {
            is PengajuanLaporanEvent.OnSearchQueryChange -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is PengajuanLaporanEvent.OnReportMoreOptionClick -> {
                _uiState.update { it.copy(isOptionSheetVisible = true, selectedReportId = event.id) }
            }
            PengajuanLaporanEvent.OnReportOptionSheetDismiss -> {
                _uiState.update { it.copy(isOptionSheetVisible = false, selectedReportId = null) }
            }
            PengajuanLaporanEvent.OnDeleteClick -> {
                _uiState.update { it.copy(isOptionSheetVisible = false, isDeleteDialogVisible = true) }
            }
            PengajuanLaporanEvent.OnDismissDeleteDialog -> {
                _uiState.update { it.copy(isDeleteDialogVisible = false) }
            }
            PengajuanLaporanEvent.OnDeleteConfirm -> deleteReport()

            PengajuanLaporanEvent.OnSubmitClick -> {
                _uiState.update { it.copy(isOptionSheetVisible = false, isSubmitDialogVisible = true) }
            }
            PengajuanLaporanEvent.OnDismissSubmitDialog -> {
                _uiState.update { it.copy(isSubmitDialogVisible = false) }
            }
            PengajuanLaporanEvent.OnSubmitConfirm -> submitReport()

            PengajuanLaporanEvent.OnDismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            PengajuanLaporanEvent.OnDismissSuccessMessage -> {
                _uiState.update { it.copy(successMessage = null) }
            }
            PengajuanLaporanEvent.OnFilterClick -> {
                _uiState.update { it.copy(isFilterSheetVisible = true) }
            }
            is PengajuanLaporanEvent.OnFilterChange -> {
                _uiState.update {
                    it.copy(
                        selectedStatus = event.status,
                        isFilterSheetVisible = false
                    )
                }
            }
            PengajuanLaporanEvent.OnDismissFilterSheet -> {
                _uiState.update { it.copy(isFilterSheetVisible = false) }
            }
        }
    }

    private fun deleteReport() {
        val id = _uiState.value.selectedReportId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isDeleteDialogVisible = false) }

            when (val result = deleteReportUseCase(id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Laporan berhasil dihapus",
                            selectedReportId = null
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }

    private fun submitReport() {
        val id = _uiState.value.selectedReportId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isOptionSheetVisible = false) }

            when (val result = submitReportUseCase(id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Laporan berhasil diajukan") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }
}