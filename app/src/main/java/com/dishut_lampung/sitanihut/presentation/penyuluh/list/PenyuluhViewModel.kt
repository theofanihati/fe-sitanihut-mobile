package com.dishut_lampung.sitanihut.presentation.penyuluh.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.GetPenyuluhUseCase
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.SyncPenyuluhDataUseCase
import com.dishut_lampung.sitanihut.presentation.penyuluh.PenyuluhEvent
import com.dishut_lampung.sitanihut.presentation.penyuluh.PenyuluhUiState
import com.dishut_lampung.sitanihut.presentation.user_management.list.UserEvent
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.PdfService
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PenyuluhViewModel @Inject constructor(
    private val getPenyuluhUseCase: GetPenyuluhUseCase,
    private val syncPenyuluhDataUseCase: SyncPenyuluhDataUseCase,
    private val userPreferences: UserPreferences,
    private val connectivityObserver: ConnectivityObserver,
    private val pdfService: PdfService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PenyuluhUiState())
    val uiState = _uiState.asStateFlow()
    private val _isOnline = MutableStateFlow(true)

    private var searchJob: Job? = null

    init {
        observeConnectivity()
        getPenyuluh()
    }

    fun onEvent(event: PenyuluhEvent) {
        when (event) {
            is PenyuluhEvent.OnSearchQueryChange -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getPenyuluh(searchQuery = event.query)
                }
            }
            PenyuluhEvent.OnRefresh -> refreshData()
            PenyuluhEvent.OnDismissError -> _uiState.update { it.copy(errorMessage = null) }
            is PenyuluhEvent.OnExportList -> {
                exportDataToPdf()
            }
        }
    }

    private fun exportDataToPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val dataToExport = uiState.value.penyuluhList
            if (dataToExport.isEmpty()) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Tidak ada data untuk diekspor")
                }
                return@launch
            }

            val headers = listOf("Nama", "NIP", "Jabatan", "Asal KPH")
            val result = pdfService.generatePdf(
                fileName = "Data_Penyuluh_${System.currentTimeMillis()}",
                reportTitle = "LAPORAN DATA PENYULUH",
                headers = headers,
                data = dataToExport,
                rowMapper = { penyuluh ->
                    listOf(
                        penyuluh.name,
                        penyuluh.identityNumber ?: "-",
                        penyuluh.position  ?: "-",
                        penyuluh.kphName ?: "-",
                    )
                }
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = result.data,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                else -> {}
            }
        }
    }

    private fun getPenyuluh(searchQuery: String = "", isRefresh: Boolean = false) {
        if (searchJob == null || searchJob?.isActive == false) {
            searchJob = viewModelScope.launch {
            }
        }

        viewModelScope.launch {
            val role = userPreferences.userRole.first() ?: ""

            _uiState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            getPenyuluhUseCase(role, searchQuery).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                penyuluhList = result.data ?: emptyList()
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }
    private fun observeConnectivity() {
        connectivityObserver.observe()
            .onEach { status ->
                _isOnline.value = status == ConnectivityObserver.Status.Available
            }
            .launchIn(viewModelScope)
    }
    private fun refreshData() {
        if (!_isOnline.value) {
            _uiState.update {
                it.copy(isRefreshing = false, errorMessage = "Tidak ada koneksi internet")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val result = syncPenyuluhDataUseCase()

            when(result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isRefreshing = false) }
                    }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isRefreshing = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}