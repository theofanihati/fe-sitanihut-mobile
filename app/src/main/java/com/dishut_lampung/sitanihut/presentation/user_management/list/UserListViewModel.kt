package com.dishut_lampung.sitanihut.presentation.user_management.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.usecase.user_management.DeleteUserUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.GetUserListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.SyncUserDataUseCase
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
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
class UserListViewModel @Inject constructor(
    private val getUserListUseCase: GetUserListUseCase,
    private val syncUserDataUseCase: SyncUserDataUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val userPreferences: UserPreferences,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val _baseState = MutableStateFlow(UserListUiState(isLoading = true))
    private val _searchQuery = MutableStateFlow("")
    private val _allPetaniData = MutableStateFlow<List<UserDetail>>(emptyList())
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
                        item.gender.contains(query, ignoreCase = true) ||
                        item.role.contains(query, ignoreCase = true)
            }
        }
        base.copy(
            userList = filteredList,
            query = query,
            isOnline = isOnline,
            userRole = role
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserListUiState(isLoading = true)
    )

    init {
        observeConnectivity()
        fetchPetaniData()
    }

    fun onEvent(event: UserEvent) {
        when (event) {
            is UserEvent.OnSearchQueryChange -> {
                _searchQuery.value = event.query
            }
            UserEvent.OnRefresh -> {
                refreshData()
            }
            is UserEvent.OnMoreOptionClick -> {
                _baseState.update {
                    it.copy(
                        isBottomSheetVisible = true,
                        selectedUserId = event.id
                    )
                }
            }
            UserEvent.OnBottomSheetDismiss -> {
                _baseState.update {
                    it.copy(
                        isBottomSheetVisible = false
                    )
                }
            }
            UserEvent.OnDeleteClick -> {
                if (_userRole.value == "penanggung jawab") return
                _baseState.update {
                    it.copy(
                        isBottomSheetVisible = false,
                        isDeleteDialogVisible = true
                    )
                }
            }
            UserEvent.OnDeleteConfirm -> {
                val idToDelete = _baseState.value.selectedUserId
                if (idToDelete != null) {
                    deletePetani(idToDelete)
                }
                _baseState.update { it.copy(isDeleteDialogVisible = false) }
            }
            UserEvent.OnDismissDeleteDialog -> {
                _baseState.update {
                    it.copy(
                        isDeleteDialogVisible = false,
                        selectedUserId = null
                    )
                }
            }
            UserEvent.OnDismissError -> {
                _baseState.update { it.copy(errorMessage = null) }
            }
            UserEvent.OnDismissSuccessMessage -> {
                _baseState.update { it.copy(successMessage = null) }
            }
            is UserEvent.OnShowUserMessage -> {
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

            when (val result = deleteUserUseCase(id)) {
                is Resource.Success -> {
                    _allPetaniData.update { currentList ->
                        currentList.filter { it.id != id }
                    }
                    _baseState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Data berhasil dihapus",
                            selectedUserId = null,
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
            _userRole.value = role
            _baseState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            getUserListUseCase(role, "").collect { result ->
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

    private fun refreshData() {
        if (!_isOnline.value) {
            _baseState.update {
                it.copy(isRefreshing = false, errorMessage = "Tidak ada koneksi internet")
            }
            return
        }

        viewModelScope.launch {
            _baseState.update { it.copy(isRefreshing = true) }
            val result = syncUserDataUseCase()

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