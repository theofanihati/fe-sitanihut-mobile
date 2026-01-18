package com.dishut_lampung.sitanihut.presentation.user_management.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.usecase.petani.DeletePetaniUseCase
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

    fun onEvent(event: UserEvent) {
        TODO()
    }
}