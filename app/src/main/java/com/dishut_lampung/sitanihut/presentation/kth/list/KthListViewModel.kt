package com.dishut_lampung.sitanihut.presentation.kth.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.usecase.kth.DeleteKthUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.SyncKthDataUseCase
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
import com.dishut_lampung.sitanihut.presentation.user_management.list.UserEvent
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.PdfService
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KthListViewModel @Inject constructor(
    private val getKthListUseCase: GetKthListUseCase,
    private val syncKthDataUseCase: SyncKthDataUseCase,
    private val deleteKthUseCase: DeleteKthUseCase,
    private val userPreferences: UserPreferences,
    private val connectivityObserver: ConnectivityObserver,
    private val pdfService: PdfService,
) : ViewModel() {

    private val _baseState = MutableStateFlow(KthUiState(isLoading = true))
    private val _searchQuery = MutableStateFlow("")
    private val _allKthData = MutableStateFlow<List<Kth>>(emptyList())
    private val _isOnline = MutableStateFlow(true)
    private val _userRole = MutableStateFlow("")

    val uiState = combine(
        _baseState,
        _allKthData,
        _searchQuery,
        _isOnline,
        _userRole
    ) { base, allData, query, isOnline, role ->

        val filteredList = if (query.isBlank()) {
            allData
        } else {
            allData.filter { item ->
                item.name.contains(query, ignoreCase = true) ||
                        item.desa.contains(query, ignoreCase = true) ||
                        item.kphName.contains(query, ignoreCase = true)
            }
        }
        base.copy(
            kthList = filteredList,
            query = query,
            isOnline = isOnline
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KthUiState(isLoading = true)
    )

    init {
        observeConnectivity()
        fetchKthData()
    }

    fun onEvent(event: KthEvent) {
        when (event) {
            is KthEvent.OnSearchQueryChange -> {
                _searchQuery.value = event.query
            }
            KthEvent.OnRefresh -> {
                refreshData()
            }
            is KthEvent.OnMoreOptionClick -> {
                _baseState.update {
                    it.copy(
                        isBottomSheetVisible = true,
                        selectedKthId = event.id
                    )
                }
            }
            KthEvent.OnBottomSheetDismiss -> {
                _baseState.update {
                    it.copy(
                        isBottomSheetVisible = false
                    )
                }
            }
            is KthEvent.OnExportList -> {
                exportDataToPdf(userId = null)
            }
            is KthEvent.OnExportDetail -> {
                exportDataToPdf(userId = event.id)
            }
            KthEvent.OnDeleteClick -> {
                if (_userRole.value == "penanggung jawab") return
                _baseState.update {
                    it.copy(
                        isBottomSheetVisible = false,
                        isDeleteDialogVisible = true
                    )
                }
            }
            KthEvent.OnDeleteConfirm -> {
                val idToDelete = _baseState.value.selectedKthId
                if (idToDelete != null) {
                    deleteKth(idToDelete)
                }
                _baseState.update { it.copy(isDeleteDialogVisible = false) }
            }
            KthEvent.OnDismissDeleteDialog -> {
                _baseState.update {
                    it.copy(
                        isDeleteDialogVisible = false,
                        selectedKthId = null
                    )
                }
            }
            KthEvent.OnDismissError -> {
                _baseState.update { it.copy(errorMessage = null) }
            }
            KthEvent.OnDismissSuccessMessage -> {
                _baseState.update { it.copy(successMessage = null) }
            }
            is KthEvent.OnShowUserMessage -> {
                viewModelScope.launch {
                    _baseState.update { it.copy(successMessage = null, errorMessage = null) }

                    if (event.type == MessageType.Success) {
                        _baseState.update { it.copy(successMessage = event.message) }
                    } else {
                        _baseState.update { it.copy(errorMessage = event.message) }
                    }
                }
            }
        }
    }

    fun exportDataToPdf(userId: String? = null) {
        viewModelScope.launch {
            _baseState.update { it.copy(isLoading = true) }

            val dataToExport = if (userId != null) {
                _allKthData.value.filter { it.id == userId }
            } else {
                uiState.value.kthList
            }

            if (dataToExport.isEmpty()) {
                _baseState.update {
                    it.copy(isLoading = false, errorMessage = "Tidak ada data untuk diekspor")
                }
                return@launch
            }

            val headers = listOf("Nama", "Desa", "Kabupaten", "Asal KPH")
            val result = pdfService.generatePdf(
                fileName = "Data_Petani_${System.currentTimeMillis()}",
                reportTitle = if (userId != null) "DETAIL DATA KTH" else "LAPORAN DATA KTH",
                headers = headers,
                data = dataToExport,
                rowMapper = { user ->
                    listOf(
                        user.name,
                        user.desa ?: "-",
                        user.kabupaten?: "-",
                        user.kphName ?: "-",
                    )
                }
            )

            when (result) {
                is Resource.Success -> {
                    _baseState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = result.data,
                            isBottomSheetVisible = false
                        )
                    }
                }
                is Resource.Error -> {
                    _baseState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                else -> {}
            }
        }
    }
    private fun deleteKth(id: String) {
        if (_userRole.value == "penanggung jawab") {
            _baseState.update { it.copy(errorMessage = "Anda tidak memiliki akses hapus") }
            return
        }

        if (!_isOnline.value) {
            _baseState.update {
                it.copy(
                    errorMessage = "Tidak ada koneksi internet",
                    isDeleteDialogVisible = false
                )
            }
            return
        }
        viewModelScope.launch {
            _baseState.update { it.copy(isLoading = true) }

            when (val result = deleteKthUseCase(id)) {
                is Resource.Success -> {
                    _allKthData.update { currentList ->
                        currentList.filter { it.id != id }
                    }
                    _baseState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Data berhasil dihapus",
                            selectedKthId = null,
                            isDeleteDialogVisible = false
                        )
                    }
                }
                is Resource.Error -> {
                    _baseState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Gagal menghapus data",
                            isDeleteDialogVisible = false
                        )
                    }
                }
                is Resource.Loading -> {}
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

    private fun fetchKthData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val role = userPreferences.userRole.first() ?: ""
            _userRole.value = role
            _baseState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            getKthListUseCase(role, "").collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _baseState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _allKthData.value = result.data ?: emptyList()
                        _baseState.update {
                            it.copy(isLoading = false, isRefreshing = false)
                        }
                    }
                    is Resource.Error -> {
                        _baseState.update {
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
    private fun refreshData() {
        if (!_isOnline.value) {
            _baseState.update {
                it.copy(isRefreshing = false, errorMessage = "Tidak ada koneksi internet")
            }
            return
        }

        viewModelScope.launch {
            _baseState.update { it.copy(isRefreshing = true) }
            val result = syncKthDataUseCase()

            when(result) {
                is Resource.Success -> {
                    _baseState.update {
                        it.copy(isRefreshing = false, successMessage = "Data berhasil diperbarui")
                    }
                }
                is Resource.Error -> {
                    _baseState.update {
                        it.copy(isRefreshing = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }
}