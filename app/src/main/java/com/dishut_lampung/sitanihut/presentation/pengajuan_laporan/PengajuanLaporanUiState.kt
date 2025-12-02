package com.dishut_lampung.sitanihut.presentation.pengajuan_laporan

import com.dishut_lampung.sitanihut.domain.model.ReportStatus

data class PengajuanLaporanUiState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedStatus: ReportStatus? = null,

    val isFilterSheetVisible: Boolean = false,
    val isOptionSheetVisible: Boolean = false,
    val selectedReportId: String? = null,

    val isDeleteDialogVisible: Boolean = false,
    val isSubmitDialogVisible: Boolean = false,

    val successMessage: String? = null,
    val errorMessage: String? = null
)

sealed class PengajuanLaporanEvent {
    data class OnSearchQueryChange(val query: String) : PengajuanLaporanEvent()
    data class OnFilterChange(val status: ReportStatus?) : PengajuanLaporanEvent()

    data class OnReportMoreOptionClick(val id: String) : PengajuanLaporanEvent()
    object OnReportOptionSheetDismiss : PengajuanLaporanEvent()
    object OnFilterClick : PengajuanLaporanEvent()
    object OnDismissFilterSheet : PengajuanLaporanEvent()

//    data class OnViewDetailClick(val reportId: String) : PengajuanLaporanEvent()
//    data class OnEditClick(val reportId: String) : PengajuanLaporanEvent()

    object OnSubmitClick : PengajuanLaporanEvent()
    object OnDeleteClick : PengajuanLaporanEvent()
//    object OnDeleteCancel : PengajuanLaporanEvent()
    object OnDeleteConfirm : PengajuanLaporanEvent()
    object OnDismissDeleteDialog : PengajuanLaporanEvent()

    object OnDismissError : PengajuanLaporanEvent()
    object OnDismissSuccessMessage : PengajuanLaporanEvent()
}