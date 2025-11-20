package com.dishut_lampung.sitanihut.presentation.home_page.petani

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.model.toUiModel
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.LogoutUseCase
import com.dishut_lampung.sitanihut.domain.usecase.home.FarmerHomeData
import com.dishut_lampung.sitanihut.domain.usecase.home.GetFarmerHomeDataUseCase
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
import javax.inject.Inject

@HiltViewModel
class HomePagePetaniViewModel @Inject constructor(
    private val getFarmerHomeDataUseCase: GetFarmerHomeDataUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val homeRepository: HomeRepository,
): ViewModel(){
    private val _dataState = getFarmerHomeDataUseCase()
        .map{it.toUiState()}
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )

    private val  _transientUiState = MutableStateFlow(HomeUiState())
    val uiState:StateFlow<HomeUiState> = combine(_transientUiState, _dataState) {currentTransient, dataUi ->
        val loadingStatus = dataUi.isLoading || currentTransient.isRefreshing
        currentTransient.copy(
            isLoading = loadingStatus,
            userProfile = dataUi.userProfile,
            reportSummary = dataUi.reportSummary,
            latestReports = dataUi.latestReports,
            generalError = currentTransient.generalError ?: dataUi.generalError,
            successMessage = currentTransient.successMessage,
            isLogoutConfirmationVisible = currentTransient.isLogoutConfirmationVisible,
            reportIdToDelete = currentTransient.reportIdToDelete,
            reportIdForOptionSheet = currentTransient.reportIdForOptionSheet,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private val _eventFlow = MutableSharedFlow<HomeUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: HomeEvent){
        when(event){
            HomeEvent.OnRefreshData -> refreshData()
            HomeEvent.OnProfileClick -> navigateToProfile()

            HomeEvent.OnLogoutClick -> updateTransientState { it.copy(isLogoutConfirmationVisible = true) }
            HomeEvent.OnLogoutCancel -> updateTransientState { it.copy(isLogoutConfirmationVisible = false) }
            HomeEvent.OnLogoutConfirm -> logout()

            HomeEvent.OnCommodityMenuClick -> emitUiEvent(HomeUiEvent.NavigateToCommodityList)
            HomeEvent.OnReportSubmissionMenuClick -> emitUiEvent(HomeUiEvent.NavigateToReportSubmission)
            HomeEvent.OnInformationMenuClick -> emitUiEvent(HomeUiEvent.NavigateToInformation)

            is HomeEvent.OnReportMoreOptionClick -> showReportOptions(event.reportId)
            HomeEvent.OnReportOptionSheetDismiss -> updateTransientState { it.copy(reportIdForOptionSheet = null) }
            is HomeEvent.OnViewDetailClick -> emitUiEvent(HomeUiEvent.NavigateToReportDetail(event.reportId))
            is HomeEvent.OnEditClick -> emitUiEvent(HomeUiEvent.NavigateToEditReport(event.reportId))

            is HomeEvent.OnDeleteClick -> showDeleteConfirmation(event.reportId)
            HomeEvent.OnDeleteCancel -> updateTransientState { it.copy(reportIdToDelete = null) }
            is HomeEvent.OnDeleteConfirm -> deleteReport(event.reportId)
            is HomeEvent.OnSubmitClick -> submitReport(event.reportId)

            HomeEvent.OnDismissError -> updateTransientState { it.copy(generalError = null) }
            HomeEvent.OnDismissSuccessMessage -> updateTransientState { it.copy(successMessage = null) }
        }
    }

    private fun refreshData(){
        updateTransientState{it.copy(isRefreshing = true)}
        viewModelScope.launch {
            try{
                homeRepository.getLatestReports().first()
            }catch(e:Exception){
                updateTransientState{it.copy(generalError = "Gagal memuat ulang data: ${e.message}")}
            }finally{
                updateTransientState{it.copy(isRefreshing=false)}
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
        updateTransientState{it.copy(
            reportIdForOptionSheet = reportId
        )}
    }

    private fun showDeleteConfirmation(reportId: String){
        updateTransientState{it.copy(
            reportIdToDelete = reportId,
            reportIdForOptionSheet = null,
        )}
    }

    private fun deleteReport(reportId: String){
        updateTransientState{it.copy(
            isLoading = true,
            reportIdToDelete = null,
        )}
        viewModelScope.launch {
            when(val result=homeRepository.deleteReport(reportId)){
                is Resource.Success -> {
                    updateTransientState{it.copy(
                        isLoading=false,
                        successMessage = "Laporan berhasil dihapus"
                    )}
                } is Resource.Error -> {
                    updateTransientState {
                        it.copy(
                            isLoading = false,
                            generalError = result.message
                        )
                    }
                } else -> updateTransientState{it.copy(isLoading = false)}
            }
        }
    }

    private fun submitReport(reportId: String){
        updateTransientState{it.copy(
            isLoading = true,
            reportIdForOptionSheet = null,
        )}
        viewModelScope.launch {
            when(val result=homeRepository.submitReport(reportId)){
                is Resource.Success -> {
                    updateTransientState{it.copy(
                        isLoading=false,
                        successMessage = "Laporan berhasil diajukan"
                    )}
                } is Resource.Error -> {
                    updateTransientState {
                        it.copy(
                            isLoading = false,
                            generalError = result.message
                        )
                    }
                } else -> updateTransientState{it.copy(isLoading=false)}
            }
        }
    }

    private fun emitUiEvent(event: HomeUiEvent){
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    private fun updateTransientState(transform: (HomeUiState) -> HomeUiState){
        _transientUiState.value = transform(_transientUiState.value)
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
