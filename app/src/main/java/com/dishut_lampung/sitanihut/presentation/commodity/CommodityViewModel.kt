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

    fun onEvent(event: CommodityEvent) {
        when (event) {
            is CommodityEvent.OnSearchQueryChange -> {
                TODO("Not yet implemented")
            }
            is CommodityEvent.OnRefresh -> {
                TODO("Not yet implemented")
            }
            CommodityEvent.OnDismissError -> {
                TODO("Not yet implemented")
            }
            CommodityEvent.OnDismissSuccessMessage -> {
                TODO("Not yet implemented")
            }
        }
    }
}