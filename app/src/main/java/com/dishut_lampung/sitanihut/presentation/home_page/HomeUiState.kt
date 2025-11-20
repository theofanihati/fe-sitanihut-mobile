package com.dishut_lampung.sitanihut.presentation.home_page

import com.dishut_lampung.sitanihut.domain.model.ReportSummary
import com.dishut_lampung.sitanihut.domain.model.ReportUiModel
import com.dishut_lampung.sitanihut.domain.model.UserProfile

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val generalError: String? = null,
    val successMessage: String? = null,

    val userProfile: UserProfile = UserProfile("Pengguna", "Petani", null),
    val reportSummary: ReportSummary = ReportSummary(0, 0, 0),
    val latestReports: List<ReportUiModel> = emptyList(),

    val isLogoutConfirmationVisible: Boolean = false,
    val reportIdToDelete: String? = null,
    val reportIdForOptionSheet: String? = null,
)

sealed class HomeEvent {
    object OnRefreshData : HomeEvent()
    object OnProfileClick : HomeEvent()

    object OnLogoutClick : HomeEvent()
    object OnLogoutConfirm : HomeEvent()
    object OnLogoutCancel : HomeEvent()

    object OnCommodityMenuClick : HomeEvent()
    object OnReportSubmissionMenuClick : HomeEvent()
    object OnInformationMenuClick : HomeEvent()

    data class OnReportMoreOptionClick(val reportId: String) : HomeEvent()
    object OnReportOptionSheetDismiss : HomeEvent()

    data class OnViewDetailClick(val reportId: String) : HomeEvent()
    data class OnEditClick(val reportId: String) : HomeEvent()
    data class OnDeleteClick(val reportId: String) : HomeEvent()
    data class OnSubmitClick(val reportId: String) : HomeEvent() // Ajukan/Submit

    object OnDeleteCancel : HomeEvent()
    data class OnDeleteConfirm(val reportId: String) : HomeEvent()

    object OnDismissError : HomeEvent()
    object OnDismissSuccessMessage : HomeEvent()
}

sealed interface HomeUiEvent {
    data class NavigateToProfile(val role: String) : HomeUiEvent
    object NavigateToLogin : HomeUiEvent
    object NavigateToCommodityList : HomeUiEvent
    object NavigateToReportSubmission : HomeUiEvent
    object NavigateToInformation : HomeUiEvent
    data class NavigateToReportDetail(val reportId: String) : HomeUiEvent
    data class NavigateToEditReport(val reportId: String) : HomeUiEvent
}