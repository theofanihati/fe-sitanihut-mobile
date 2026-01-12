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

    fun onEvent(event: PetaniEvent) {
        TODO()
    }
}