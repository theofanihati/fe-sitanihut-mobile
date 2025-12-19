package com.dishut_lampung.sitanihut.presentation.report.detail

import com.dishut_lampung.sitanihut.domain.model.ReportDetail

enum class ReportAction {
    VERIFY, APPROVE, REJECT
}
sealed interface ReportDetailUiState {
    data object Loading : ReportDetailUiState

    data class Success(
        val data: ReportDetail,
        val isRefreshing: Boolean = false,
        val canVerify: Boolean = false,
        val canApprove: Boolean = false,
        val canReject: Boolean = false,
        val isActionLoading: Boolean = false,
        val actionMessage: String? = null,
        val pendingAction: ReportAction? = null,
    ) : ReportDetailUiState


    data class Error(val message: String) : ReportDetailUiState
}

sealed class ReportDetailEvent {
    data object OnVerifyClick : ReportDetailEvent()
    data object OnRejectClick : ReportDetailEvent()
    data object OnApproveClick : ReportDetailEvent()
    data object OnConfirmDialog : ReportDetailEvent()
    data object OnDismissDialog : ReportDetailEvent()
    data object OnDismissMessage : ReportDetailEvent()
    data object OnRefresh : ReportDetailEvent()
}