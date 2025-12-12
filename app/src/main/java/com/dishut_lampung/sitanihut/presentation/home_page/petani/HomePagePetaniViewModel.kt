package com.dishut_lampung.sitanihut.presentation.home_page.petani

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportStatus
import com.dishut_lampung.sitanihut.domain.model.ReportUiModel
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.LogoutUseCase
import com.dishut_lampung.sitanihut.domain.usecase.home.FarmerHomeData
import com.dishut_lampung.sitanihut.domain.usecase.home.GetPetaniHomeDataUseCase
import com.dishut_lampung.sitanihut.presentation.home_page.HomeEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiState
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomePagePetaniViewModel @Inject constructor(
    private val getPetaniHomeDataUseCase: GetPetaniHomeDataUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val homeRepository: HomeRepository,
): ViewModel(){

    private val _dataState = getPetaniHomeDataUseCase()
        .map{it.toUiState()}
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )

    private val  _temporatyUiState = MutableStateFlow(HomeUiState())

    val uiState:StateFlow<HomeUiState> = combine(_temporatyUiState, _dataState) {currentTemporary, dataUi ->
        val loadingStatus = dataUi.isLoading || currentTemporary.isRefreshing
        currentTemporary.copy(
            isLoading = loadingStatus,
            userProfile = dataUi.userProfile,
            reportSummary = dataUi.reportSummary,
            latestReports = dataUi.latestReports,
            generalError = currentTemporary.generalError ?: dataUi.generalError,
            successMessage = currentTemporary.successMessage,
            isLogoutConfirmationVisible = currentTemporary.isLogoutConfirmationVisible,
            reportIdToDelete = currentTemporary.reportIdToDelete,
            reportIdToSubmit = currentTemporary.reportIdToSubmit,
            reportIdForOptionSheet = currentTemporary.reportIdForOptionSheet,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    private val _eventFlow = MutableSharedFlow<HomeUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: HomeEvent){
        when(event){
            HomeEvent.OnRefreshData -> refreshData()
            HomeEvent.OnProfileClick -> navigateToProfile()

            HomeEvent.OnLogoutClick -> updateTemporaryState { it.copy(isLogoutConfirmationVisible = true) }
            HomeEvent.OnLogoutCancel -> updateTemporaryState { it.copy(isLogoutConfirmationVisible = false) }
            HomeEvent.OnLogoutConfirm -> logout()

            is HomeEvent.OnReportMoreOptionClick -> showReportOptions(event.reportId)
            HomeEvent.OnReportOptionSheetDismiss -> updateTemporaryState { it.copy(reportIdForOptionSheet = null) }
            is HomeEvent.OnViewDetailClick -> emitUiEvent(HomeUiEvent.NavigateToReportDetail(event.reportId))
            is HomeEvent.OnEditClick -> emitUiEvent(HomeUiEvent.NavigateToEditReport(event.reportId))

            is HomeEvent.OnDeleteClick -> showDeleteConfirmation(event.reportId)
            HomeEvent.OnDeleteCancel -> updateTemporaryState { it.copy(reportIdToDelete = null) }
            is HomeEvent.OnDeleteConfirm -> deleteReport()

            is HomeEvent.OnSubmitClick -> showSubmitConfirmation(event.reportId)
            HomeEvent.OnSubmitCancel -> updateTemporaryState { it.copy(reportIdToSubmit = null) }
            is HomeEvent.OnSubmitConfirm -> submitReport()

            HomeEvent.OnDismissError -> updateTemporaryState { it.copy(generalError = null) }
            HomeEvent.OnDismissSuccessMessage -> updateTemporaryState { it.copy(successMessage = null) }
        }
    }

    private fun refreshData(){
        updateTemporaryState{it.copy(isRefreshing = true)}
        viewModelScope.launch {
            try{
                homeRepository.getLatestReports().first()
            }catch(e:Exception){
                updateTemporaryState{it.copy(generalError = "Gagal memuat ulang data: ${e.message}")}
            }finally{
                updateTemporaryState{it.copy(isRefreshing=false)}
            }
        }
    }

    private fun navigateToProfile(){
        val role = _dataState.value.userProfile.role
        emitUiEvent(HomeUiEvent.NavigateToProfile(role))
    }

    private fun logout(){
        viewModelScope.launch {
            logoutUseCase()
            emitUiEvent(HomeUiEvent.NavigateToLogin)
        }
    }

    private fun showReportOptions(reportId:String){
        updateTemporaryState{it.copy(
            reportIdForOptionSheet = reportId
        )}
    }

    private fun showDeleteConfirmation(reportId: String){
        updateTemporaryState{it.copy(
            reportIdToDelete = reportId,
            reportIdForOptionSheet = null,
        )}
    }

    private fun showSubmitConfirmation(reportId: String){
        updateTemporaryState{it.copy(
            reportIdToSubmit = reportId,
            reportIdForOptionSheet = null,
        )}
    }

    private fun deleteReport(){
        val reportId = _temporatyUiState.value.reportIdToDelete ?: return

        updateTemporaryState{it.copy(
            isLoading = true,
            reportIdToDelete = null,
        )}
        viewModelScope.launch {
            when(val result=homeRepository.deleteReport(reportId)){
                is Resource.Success -> {
                    updateTemporaryState{it.copy(
                        isLoading=false,
                        successMessage = "Laporan berhasil dihapus"
                    )}
                    onEvent(HomeEvent.OnRefreshData)
                } is Resource.Error -> {
                    updateTemporaryState {
                        it.copy(
                            isLoading = false,
                            generalError = result.message
                        )
                    }
                } else -> updateTemporaryState{it.copy(isLoading = false)}
            }
        }
    }

    private fun submitReport(){
        val reportId = _temporatyUiState.value.reportIdToSubmit ?: return
        updateTemporaryState{it.copy(
            isLoading = true,
            reportIdForOptionSheet = null,
        )}
        viewModelScope.launch {
            when(val result=homeRepository.submitReport(reportId)){
                is Resource.Success -> {
                    updateTemporaryState{it.copy(
                        isLoading=false,
                        successMessage = "Laporan berhasil diajukan"
                    )}
                    onEvent(HomeEvent.OnRefreshData)
                } is Resource.Error -> {
                updateTemporaryState {
                    it.copy(
                        isLoading = false,
                        generalError = result.message
                    )
                }
            } else -> updateTemporaryState{it.copy(isLoading=false)}
            }
        }
    }

    private fun emitUiEvent(event: HomeUiEvent){
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    private fun updateTemporaryState(transform: (HomeUiState) -> HomeUiState){
        _temporatyUiState.value = transform(_temporatyUiState.value)
    }

    private fun FarmerHomeData.toUiState(): HomeUiState{
        return HomeUiState(
            userProfile = this.userProfile,
            reportSummary = this.summary,
            latestReports = this.latestReports.map{it.toUiModel()},
            isLoading = false,
            generalError = null,
        )
    }
}

fun Report.toUiModel(): ReportUiModel {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    val nteFormatted = try {
        formatter.format(this.totalTransaction.toDouble())
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

    val isEditable = this.status == ReportStatus.DRAFT || this.status == ReportStatus.REJECTED
    val isDeletable = this.status == ReportStatus.DRAFT || this.status == ReportStatus.REJECTED
    val monthSentenceCase = this.monthPeriod.replaceFirstChar { it.titlecase() }
    val periodTitle = "Laporan Periode ${monthSentenceCase} ${this.period}"

    return ReportUiModel(
        id = this.id,
        periodTitle = periodTitle,
        dateDisplay = this.submissionDate,
        nteDisplay = nteFormatted,
        statusDisplay = statusText,
        isEditable = isEditable,
        domainStatus = this.status,
        isDeletable = isDeletable
    )
}