package com.dishut_lampung.sitanihut.presentation.home_page.penyuluh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.ReportSummary
import com.dishut_lampung.sitanihut.domain.model.ReportUiModel
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.LogoutUseCase
import com.dishut_lampung.sitanihut.presentation.home_page.HomeEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiState
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomePagePenyuluhViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(isLoading = true))
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<HomeUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadData()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnRefreshData -> loadData(isRefreshing = true)
            HomeEvent.OnProfileClick -> navigateToProfile()

            HomeEvent.OnLogoutClick -> _state.update { it.copy(isLogoutConfirmationVisible = true) }
            HomeEvent.OnLogoutCancel -> _state.update { it.copy(isLogoutConfirmationVisible = false) }
            HomeEvent.OnLogoutConfirm -> logout()

            is HomeEvent.OnViewDetailClick -> emitUiEvent(HomeUiEvent.NavigateToReportDetail(event.reportId))

            HomeEvent.OnDismissError -> _state.update { it.copy(generalError = null) }
            HomeEvent.OnDismissSuccessMessage -> _state.update { it.copy(successMessage = null) }

            else -> {}
        }
    }

    private fun loadData(isRefreshing: Boolean = false) {
        if (isRefreshing) _state.update { it.copy(isRefreshing = true) }

        viewModelScope.launch {
            val profileFlow = homeRepository.getUserProfile()
            val pendingFlow = homeRepository.getReportsByStatus("menunggu")
            val approvedFlow = homeRepository.getReportsByStatus("disetujui")

            combine(profileFlow, pendingFlow, approvedFlow) { profile, pendingRes, approvedRes ->

                val pendingList = when (pendingRes) {
                    is Resource.Success -> pendingRes.data ?: emptyList()
                    is Resource.Error -> {
                        _state.update { it.copy(generalError = pendingRes.message) }
                        emptyList()
                    }
                    is Resource.Loading -> emptyList()
                }

                val approvedList = when (approvedRes) {
                    is Resource.Success -> approvedRes.data ?: emptyList()
                    is Resource.Error -> {
                        _state.update { it.copy(generalError = approvedRes.message) }
                        emptyList()
                    }
                    is Resource.Loading -> emptyList()
                }

                val errorMsg = pendingRes.message ?: approvedRes.message

                val summary = ReportSummary(
                    pendingCount = pendingList.size,
                    verifiedcount = 0,
                    approvedCount = approvedList.size,
                    rejectedCount = 0
                )

                HomeUiState(
                    isLoading = false,
                    isRefreshing = false,
                    userProfile = profile,
                    reportSummary = summary,
                    latestReports = pendingList.map { it.toPenyuluhUiModel() },
                    generalError = if (pendingList.isEmpty() && approvedList.isEmpty()) errorMsg else null
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun navigateToProfile() {
        val role = _state.value.userProfile.role
        emitUiEvent(HomeUiEvent.NavigateToProfile(role))
    }

    private fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            emitUiEvent(HomeUiEvent.NavigateToLogin)
        }
    }

    private fun emitUiEvent(event: HomeUiEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    private fun Report.toPenyuluhUiModel(): ReportUiModel {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

        val nteFormatted = try {
            formatter.format(this.totalTransaction)
        } catch (e: Exception) {
            "Rp0"
        }

        val statusText = when (this.status) {
            ReportStatus.APPROVED -> "Disetujui"
            ReportStatus.REJECTED -> "Ditolak"
            ReportStatus.VERIFIED -> "Diverifikasi"
            ReportStatus.PENDING -> "Menunggu"
            ReportStatus.DRAFT -> "Belum diajukan"
        }

        return ReportUiModel(
            id = this.id,
            periodTitle = "Laporan ${this.monthPeriod} ${this.period}",
            dateDisplay = this.submissionDate,
            nteDisplay = nteFormatted,
            statusDisplay = statusText,
            isEditable = false,
            isDeletable = false,
            domainStatus = this.status
        )
    }
}