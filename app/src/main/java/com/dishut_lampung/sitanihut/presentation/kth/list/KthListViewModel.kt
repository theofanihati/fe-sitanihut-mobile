package com.dishut_lampung.sitanihut.presentation.kth.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.usecase.kth.DeleteKthUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
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
    private val deleteKthUseCase: DeleteKthUseCase,
    private val userPreferences: UserPreferences,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _baseState = MutableStateFlow(KthUiState(isLoading = true))
    private val _searchQuery = MutableStateFlow("")
    private val _allKthData = MutableStateFlow<List<Kth>>(emptyList())
    private val _isOnline = MutableStateFlow(true)

    val uiState = combine(
        _baseState,
        _allKthData,
        _searchQuery,
        _isOnline
    ) { base, allData, query, isOnline ->

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
                fetchKthData(isRefresh = true)
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
            KthEvent.OnDeleteClick -> {
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

    private fun deleteKth(id: String) {
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
}