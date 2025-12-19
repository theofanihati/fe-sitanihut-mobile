package com.dishut_lampung.sitanihut.presentation.report.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.usecase.report.GetReportDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.report.ReviewReportUseCase
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    private val getReportDetailUseCase: GetReportDetailUseCase,
    private val getReviewReportUseCase: ReviewReportUseCase,
    private val userPreferences: UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reportId: String = checkNotNull(savedStateHandle["reportId"])

    private val _uiState = MutableStateFlow<ReportDetailUiState>(ReportDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        getReportDetail()
    }

    fun getReportDetail() {
        viewModelScope.launch {
            combine(
                getReportDetailUseCase(reportId),
                userPreferences.userRole
            ) { result, role ->
                when (result) {
                    is Resource.Loading -> ReportDetailUiState.Loading
                    is Resource.Success -> {
                        val data = result.data ?: return@combine ReportDetailUiState.Error("Data tidak ditemukan")
                        val isPenyuluh = role.equals("penyuluh", ignoreCase = true)
                        val isPj = role.equals("pj", ignoreCase = true) ||
                                role.equals("penanggung jawab", ignoreCase = true) ||
                                role.equals("penanggung-jawab", ignoreCase = true)

                        val status = data.status

                        val canVerify = isPenyuluh && status == ReportStatus.PENDING
                        val canApprove = isPj && status == ReportStatus.VERIFIED
                        val canReject = (isPenyuluh && status == ReportStatus.PENDING) ||
                                (isPj && status == ReportStatus.VERIFIED)

                        ReportDetailUiState.Success(
                            data = data,
                            canVerify = canVerify,
                            canApprove = canApprove,
                            canReject = canReject
                        )
                    }
                    is Resource.Error -> ReportDetailUiState.Error(result.message ?: "Terjadi kesalahan")
                }
            }.collect { state ->
                _uiState.update { currentState ->
                    if (currentState is ReportDetailUiState.Success && state is ReportDetailUiState.Success) {
                        state.copy(
                            isActionLoading = currentState.isActionLoading,
                            actionMessage = currentState.actionMessage
                        )
                        return@collect
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun onEvent(event: ReportDetailEvent) {
        when (event) {
            ReportDetailEvent.OnVerifyClick -> submitReview(ReportStatus.VERIFIED, "Laporan berhasil diverifikasi")
            ReportDetailEvent.OnApproveClick -> submitReview(ReportStatus.APPROVED, "Laporan berhasil disetujui")
            ReportDetailEvent.OnRejectClick -> submitReview(ReportStatus.REJECTED, "Laporan berhasil ditolak")
            ReportDetailEvent.OnRefresh -> getReportDetail()
            ReportDetailEvent.OnDismissMessage -> {
                _uiState.update {
                    if (it is ReportDetailUiState.Success) it.copy(actionMessage = null) else it
                }
            }
        }
    }
    private fun submitReview(status: ReportStatus, successMessage: String) {
        viewModelScope.launch {
            _uiState.update {
                if (it is ReportDetailUiState.Success) it.copy(isActionLoading = true) else it
            }

            val role = userPreferences.userRole.first() ?: ""

            when (val result = getReviewReportUseCase(reportId, status, role)) {
                is Resource.Success -> {
                    _uiState.update {
                        if (it is ReportDetailUiState.Success) {
                            it.copy(
                                isActionLoading = false,
                                actionMessage = successMessage
                            )
                        } else it
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        if (it is ReportDetailUiState.Success) {
                            it.copy(
                                isActionLoading = false,
                                actionMessage = result.message
                            )
                        } else it
                    }
                }
                else -> {}
            }
        }
    }
}