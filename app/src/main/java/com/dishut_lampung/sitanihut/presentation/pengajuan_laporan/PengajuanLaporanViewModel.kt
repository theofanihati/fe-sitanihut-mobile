package com.dishut_lampung.sitanihut.presentation.pengajuan_laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.dishut_lampung.sitanihut.domain.usecase.report.DeleteReportUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportsUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.SubmitReportUseCase
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val privateUiState = MutableStateFlow(PengajuanLaporanUiState())
    val uiState = privateUiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val reportPagingFlow = privateUiState
        .map { it.searchQuery to it.selectedStatus }
        .distinctUntilChanged()
        .flatMapLatest { (query, status) ->
            getReportsUseCase(query, status)
                .cachedIn(viewModelScope)
        }

    fun onEvent(e: PengajuanLaporanEvent) {
        when (e) {
            is PengajuanLaporanEvent.OnSearchQueryChange -> {
                privateUiState.value = privateUiState.value.copy(searchQuery = e.query)
            }
            is PengajuanLaporanEvent.OnFilterChange -> {
                privateUiState.value = privateUiState.value.copy(selectedStatus = e.status)
            }
            is PengajuanLaporanEvent.OnReportMoreOptionClick -> {
                privateUiState.value = privateUiState.value.copy(isOptionSheetVisible = true, selectedReportId = e.id)
            }
            PengajuanLaporanEvent.OnReportOptionSheetDismiss -> {
                privateUiState.value = privateUiState.value.copy(isOptionSheetVisible = false, selectedReportId = null)
            }
            PengajuanLaporanEvent.OnDeleteClick -> {
                privateUiState.value =privateUiState.value.copy(isOptionSheetVisible = false, isDeleteDialogVisible = true)
            }
            PengajuanLaporanEvent.OnDismissDeleteDialog -> {
                privateUiState.value =privateUiState.value.copy(isDeleteDialogVisible = false)
            }
            PengajuanLaporanEvent.OnDeleteConfirm -> EXECUTE_DELETE_PROCESS()
            PengajuanLaporanEvent.OnSubmitClick -> EXECUTE_SUBMIT_PROCESS()

            PengajuanLaporanEvent.OnDismissError -> {
                privateUiState.value =privateUiState.value.copy(errorMessage = null)
            }
            PengajuanLaporanEvent.OnDismissSuccessMessage -> {
                privateUiState.value =privateUiState.value.copy(successMessage = null)
            }
        }
    }

    private fun EXECUTE_DELETE_PROCESS() {
        val id = privateUiState.value.selectedReportId ?: return

        viewModelScope.launch {
            privateUiState.update { it.copy(isLoading = true, isDeleteDialogVisible = false) }

            when (val result = deleteReportUseCase(id)) {
                is Resource.Success -> {
                    privateUiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Laporan berhasil dihapus",
                            selectedReportId = null
                        )
                    }
                }

                is Resource.Error -> {
                    privateUiState.update {
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

    private fun EXECUTE_SUBMIT_PROCESS() {
        val id = privateUiState.value.selectedReportId ?: return

        viewModelScope.launch {
            privateUiState.update { it.copy(isLoading = true, isOptionSheetVisible = false) } // Tutup sheet langsung

            when (val result = submitReportUseCase(id)) {
                is Resource.Success -> {
                    privateUiState.update { it.copy(isLoading = false, successMessage = "Laporan berhasil diajukan") }
                }
                is Resource.Error -> {
                    privateUiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {}
            }
        }
    }
}