package com.dishut_lampung.sitanihut.presentation.report.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.usecase.report.DeleteReportUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportsUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.SubmitReportUseCase
import com.dishut_lampung.sitanihut.presentation.home_page.petani.toUiModel
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportListViewModel @Inject constructor(
    private val getReportsUseCase: GetReportsUseCase,
    private val deleteReportUseCase: DeleteReportUseCase,
    private val submitReportUseCase: SubmitReportUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportListUiState())
    val uiState = _uiState.asStateFlow()
    private val userRoleFlow = userPreferences.userRole

    init {
        viewModelScope.launch {
            userPreferences.userRole.collect { role ->
                val isUserPetani = role.equals("petani", ignoreCase = true)
                _uiState.update {
                    it.copy(isPetani = isUserPetani)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val reportPagingFlow = combine(
        _uiState,
        userRoleFlow
    ) { state, role ->
        val query = state.searchQuery
        val userSelectedStatus = state.selectedStatus
        val statusFilter = userSelectedStatus

        Triple(query, statusFilter, role)
    }
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { (query, status, role) ->
            getReportsUseCase(query, status)
                .map { pagingData ->
                    pagingData.filter { report ->
                        if (status != null) return@filter true

                        when {
                            role.equals("penyuluh", ignoreCase = true) -> {
                                !report.status.equals("belum diajukan")
                            }

                            role.equals("penanggung jawab", ignoreCase = true) ||
                                    role.equals("penanggung-jawab", ignoreCase = true) -> {
                                !report.status.equals("belum diajukan") &&
                                        !report.status.equals("menunggu")
                            }
                            else -> true
                        }
                    }
                        .map { report -> report.toUiModel() }
                }
                .cachedIn(viewModelScope)
        }

    fun onEvent(event: ReportListEvent) {
        when (event) {
            is ReportListEvent.OnSearchQueryChange -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is ReportListEvent.OnReportMoreOptionClick -> {
                _uiState.update { it.copy(isOptionSheetVisible = true, selectedReportId = event.id) }
            }
            ReportListEvent.OnReportOptionSheetDismiss -> {
                _uiState.update { it.copy(isOptionSheetVisible = false, selectedReportId = null) }
            }
            ReportListEvent.OnDeleteClick -> {
                _uiState.update { it.copy(isOptionSheetVisible = false, isDeleteDialogVisible = true) }
            }
            ReportListEvent.OnDismissDeleteDialog -> {
                _uiState.update { it.copy(isDeleteDialogVisible = false) }
            }
            ReportListEvent.OnDeleteConfirm -> deleteReport()

            ReportListEvent.OnSubmitClick -> {
                _uiState.update { it.copy(isOptionSheetVisible = false, isSubmitDialogVisible = true) }
            }
            ReportListEvent.OnDismissSubmitDialog -> {
                _uiState.update { it.copy(isSubmitDialogVisible = false) }
            }
            ReportListEvent.OnSubmitConfirm -> submitReport()

            ReportListEvent.OnDismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            ReportListEvent.OnDismissSuccessMessage -> {
                _uiState.update { it.copy(successMessage = null) }
            }
            ReportListEvent.OnFilterClick -> {
                _uiState.update { it.copy(isFilterSheetVisible = true) }
            }
            is ReportListEvent.OnFilterChange -> {
                _uiState.update {
                    it.copy(
                        selectedStatus = event.status,
                        isFilterSheetVisible = false
                    )
                }
            }
            ReportListEvent.OnDismissFilterSheet -> {
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