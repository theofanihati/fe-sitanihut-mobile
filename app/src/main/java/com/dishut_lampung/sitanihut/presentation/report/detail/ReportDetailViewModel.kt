package com.dishut_lampung.sitanihut.presentation.report.detail

import android.util.Log
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
                            successMessage = currentState.successMessage,
                            errorMessage = null,
                            pendingAction = currentState.pendingAction
                        )
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun onEvent(event: ReportDetailEvent) {
        when (event) {
            ReportDetailEvent.OnVerifyClick -> setPendingAction(ReportAction.VERIFY)
            ReportDetailEvent.OnApproveClick -> setPendingAction(ReportAction.APPROVE)
            ReportDetailEvent.OnRejectClick -> setPendingAction(ReportAction.REJECT)
            ReportDetailEvent.OnConfirmDialog -> executePendingAction()
            ReportDetailEvent.OnDismissDialog -> setPendingAction(null)
            ReportDetailEvent.OnRefresh -> getReportDetail()
            ReportDetailEvent.OnDismissMessage -> {
                _uiState.update {
                    if (it is ReportDetailUiState.Success) it.copy(successMessage = null, errorMessage = null) else it
                }
            }
        }
    }
    private fun setPendingAction(action: ReportAction?) {
        _uiState.update {
            if (it is ReportDetailUiState.Success) it.copy(pendingAction = action) else it
        }
    }
    private fun executePendingAction() {
        val currentState = _uiState.value
        if (currentState is ReportDetailUiState.Success && currentState.pendingAction != null) {
            val action = currentState.pendingAction
            setPendingAction(null)

//            Log.d("ReportDetailVM", "Action executed: $action")

            when (action) {
                ReportAction.VERIFY -> submitReview(ReportStatus.VERIFIED, "Laporan berhasil diverifikasi")
                ReportAction.APPROVE -> submitReview(ReportStatus.APPROVED, "Laporan berhasil disetujui")
                ReportAction.REJECT -> submitReview(ReportStatus.REJECTED, "Laporan berhasil ditolak")
            }
        }
    }
    private fun submitReview(status: ReportStatus, successMessage: String) {
//        Log.d("ReportDetailVM", "Starting submitReview for status: $status")
        viewModelScope.launch {
            _uiState.update {
                if (it is ReportDetailUiState.Success) it.copy(isActionLoading = true) else it
            }

            val role = userPreferences.userRole.first() ?: ""
//            Log.d("ReportDetailVM", "Role fetched: $role")

            when (val result = getReviewReportUseCase(reportId, status, role)) {
                is Resource.Success -> {
//                    Log.d("ReportDetailVM", "UseCase Success")
                    _uiState.update {
                        if (it is ReportDetailUiState.Success) {
                            it.copy(
                                isActionLoading = false,
                                successMessage = successMessage,
                                errorMessage = null
                            )
                        } else it
                    }
                    getReportDetail()
                }
                is Resource.Error -> {
//                    Log.e("ReportDetailVM", "UseCase Error: ${result.message}")
                    _uiState.update {
                        if (it is ReportDetailUiState.Success) {
                            it.copy(
                                isActionLoading = false,
                                errorMessage = result.message ?: "Gagal memproses tindakan",
                                successMessage = null
                            )
                        } else it
                    }
                }
                else -> {}
            }
        }
    }
}