package com.dishut_lampung.sitanihut.presentation.petani.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.usecase.petani.DeletePetaniUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.GetPetaniListUseCase
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.Resource
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

class PetaniListViewModel @Inject constructor(
    private val getPetaniListUseCase: GetPetaniListUseCase,
    private val deletePetaniUseCase: DeletePetaniUseCase,
    private val userPreferences: UserPreferences,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _baseState = MutableStateFlow(PetaniListUiState(isLoading = true))
    private val _searchQuery = MutableStateFlow("")
    private val _allPetaniData = MutableStateFlow<List<Petani>>(emptyList())
    private val _isOnline = MutableStateFlow(true)
    private val _userRole = MutableStateFlow("")

    val uiState = combine(
        _baseState,
        _allPetaniData,
        _searchQuery,
        _isOnline,
        _userRole
    ) { base, allData, query, isOnline, role ->

        val filteredList = if (query.isBlank()) {
            allData
        } else {
            allData.filter { item ->
                item.name.contains(query, ignoreCase = true) ||
                        item.identityNumber.contains(query, ignoreCase = true) ||
                        item.kphName.contains(query, ignoreCase = true) ||
                        item.kthName.contains(query, ignoreCase = true)
            }
        }
        base.copy(
            petaniList = filteredList,
            query = query,
            isOnline = isOnline,
            userRole = role
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PetaniListUiState(isLoading = true)
    )

    init {
        observeConnectivity()
        fetchPetaniData()
    }

    fun onEvent(event: PetaniEvent) {
        when (event) {
            is PetaniEvent.OnSearchQueryChange -> {
                _searchQuery.value = event.query
            }
            PetaniEvent.OnRefresh -> {
                fetchPetaniData(isRefresh = true)
            }
            is PetaniEvent.OnMoreOptionClick -> {
                _baseState.update {
                    it.copy(
                        isBottomSheetVisible = true,
                        selectedPetaniId = event.id
                    )
                }
            }
            PetaniEvent.OnBottomSheetDismiss -> {
                _baseState.update {
                    it.copy(
                        isBottomSheetVisible = false
                    )
                }
            }
            PetaniEvent.OnDeleteClick -> {
                if (_userRole.value == "penanggung jawab") return
                _baseState.update {
                    it.copy(
                        isBottomSheetVisible = false,
                        isDeleteDialogVisible = true
                    )
                }
            }
            PetaniEvent.OnDeleteConfirm -> {
                val idToDelete = _baseState.value.selectedPetaniId
                if (idToDelete != null) {
                    deletePetani(idToDelete)
                }
                _baseState.update { it.copy(isDeleteDialogVisible = false) }
            }
            PetaniEvent.OnDismissDeleteDialog -> {
                _baseState.update {
                    it.copy(
                        isDeleteDialogVisible = false,
                        selectedPetaniId = null
                    )
                }
            }
            PetaniEvent.OnDismissError -> {
                _baseState.update { it.copy(errorMessage = null) }
            }
            PetaniEvent.OnDismissSuccessMessage -> {
                _baseState.update { it.copy(successMessage = null) }
            }
            is PetaniEvent.OnShowUserMessage -> {
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

    private fun deletePetani(id: String) {
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

            when (val result = deletePetaniUseCase(id)) {
                is Resource.Success -> {
                    _allPetaniData.update { currentList ->
                        currentList.filter { it.id != id }
                    }
                    _baseState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Data berhasil dihapus",
                            selectedPetaniId = null,
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

    private fun fetchPetaniData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val role = userPreferences.userRole.first() ?: ""

            _baseState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            getPetaniListUseCase(role, "").collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _baseState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _allPetaniData.value = result.data ?: emptyList()
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