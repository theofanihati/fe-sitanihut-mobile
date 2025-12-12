package com.dishut_lampung.sitanihut.presentation.report_submission.detail

import com.dishut_lampung.sitanihut.domain.model.ReportDetail

sealed interface ReportDetailUiState {
    data object Loading : ReportDetailUiState

    data class Success(
        val data: ReportDetail,
        val isRefreshing: Boolean = false
    ) : ReportDetailUiState

    data class Error(val message: String) : ReportDetailUiState
}