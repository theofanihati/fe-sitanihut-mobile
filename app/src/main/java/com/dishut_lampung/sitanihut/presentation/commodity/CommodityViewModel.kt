package com.dishut_lampung.sitanihut.presentation.commodity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.usecase.commodity.GetCommoditiesUseCase
import com.dishut_lampung.sitanihut.presentation.pengajuan_laporan.PengajuanLaporanEvent
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

@HiltViewModel
class CommodityViewModel @Inject constructor(
    private val getCommoditiesUseCase: GetCommoditiesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommodityUiState())
    val uiState: StateFlow<CommodityUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        getCommodities()
    }

    fun onEvent(event: CommodityEvent) {
        when (event) {
            is CommodityEvent.OnSearchQueryChange -> {
                _uiState.value = _uiState.value.copy(query = event.query)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getCommodities(event.query)
                }
            }
            is CommodityEvent.OnRefresh -> {
                getCommodities(_uiState.value.query)
            }

            CommodityEvent.OnDismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            CommodityEvent.OnDismissSuccessMessage -> {
                _uiState.update { it.copy(successMessage = null) }
            }
        }
    }

    private fun getCommodities(query: String = "") {
        getCommoditiesUseCase(query).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        items = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        items = result.data ?: emptyList(),
                        isLoading = false,
                        error = result.message ?: "Terjadi kesalahan"
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }.launchIn(viewModelScope)
    }
}