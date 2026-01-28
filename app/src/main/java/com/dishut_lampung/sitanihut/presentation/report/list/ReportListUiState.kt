package com.dishut_lampung.sitanihut.presentation.report.list

import com.dishut_lampung.sitanihut.domain.model.ReportStatus

data class ReportListUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedStatus: ReportStatus? = null,
    val isPetani: Boolean = false,
    val isPj: Boolean = false,
    val isFilterSheetVisible: Boolean = false,
    val isOptionSheetVisible: Boolean = false,
    val selectedReportId: String? = null,

    val isDeleteDialogVisible: Boolean = false,
    val isSubmitDialogVisible: Boolean = false,

    val successMessage: String? = null,
    val errorMessage: String? = null
)

sealed class ReportListEvent {
    data class OnSearchQueryChange(val query: String) : ReportListEvent()
    data class OnFilterChange(val status: ReportStatus?) : ReportListEvent()

    data class OnReportMoreOptionClick(val id: String) : ReportListEvent()
    object OnReportOptionSheetDismiss : ReportListEvent()
    object OnFilterClick : ReportListEvent()
    object OnDismissFilterSheet : ReportListEvent()

//    data class OnViewDetailClick(val reportId: String) : PengajuanLaporanEvent()
//    data class OnEditClick(val reportId: String) : PengajuanLaporanEvent()

    object OnSubmitClick : ReportListEvent()
    object OnSubmitConfirm : ReportListEvent()
    object OnDismissSubmitDialog : ReportListEvent()

    object OnDeleteClick : ReportListEvent()
//    object OnDeleteCancel : ReportListEvent()
    object OnDeleteConfirm : ReportListEvent()
    object OnDismissDeleteDialog : ReportListEvent()

    object OnDismissError : ReportListEvent()
    object OnDismissSuccessMessage : ReportListEvent()
}