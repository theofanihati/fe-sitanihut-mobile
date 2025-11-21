package com.dishut_lampung.sitanihut.presentation.home_page.penyuluh

import androidx.lifecycle.ViewModel
import com.dishut_lampung.sitanihut.domain.model.Report
import com.dishut_lampung.sitanihut.domain.model.ReportUiModel
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.LogoutUseCase
import com.dishut_lampung.sitanihut.presentation.home_page.HomeEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiEvent
import com.dishut_lampung.sitanihut.presentation.home_page.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
        // TODO: blum la boy
    }

    fun onEvent(event: HomeEvent) {
        // TODO: Implementasi event handler
    }

    private fun loadData(isRefreshing: Boolean = false) {
        // TODO: Implementasi logika combine flow dari repository
    }

    private fun navigateToProfile() {
        // TODO: Implementasi navigasi ke profil
    }

    private fun logout() {
        // TODO: Implementasi use case logout
    }

    private fun emitUiEvent(event: HomeUiEvent) {
        // TODO: Emit event ke UI
    }

    private fun Report.toPenyuluhUiModel(): ReportUiModel {
        throw NotImplementedError("Mapper belum diimplementasi")
    }
}