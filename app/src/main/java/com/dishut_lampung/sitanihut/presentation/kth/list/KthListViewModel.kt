package com.dishut_lampung.sitanihut.presentation.kth.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.presentation.kth.KthEvent
import com.dishut_lampung.sitanihut.presentation.kth.KthUiState
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
    private val userPreferences: UserPreferences,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _allKthData = MutableStateFlow<List<Kth>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isOnline = MutableStateFlow(true)

    val uiState = combine(
        _isLoading,
        _allKthData,
        _searchQuery,
        _error,
        _isOnline
    ) { isLoading, allData, query, error, isOnline ->

        val filteredList = if (query.isBlank()) {
            allData
        } else {
            allData.filter { item ->
                item.name.contains(query, ignoreCase = true) ||
                        item.desa.contains(query, ignoreCase = true) ||
                        item.kphName.contains(query, ignoreCase = true)
            }
        }

        KthUiState(
            isLoading = isLoading,
            kthList = filteredList,
            error = error,
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
        TODO()
    }

    private fun observeConnectivity() {
        TODO()
    }

    private fun fetchKthData(isRefresh: Boolean = false) {
        TODO()
    }
}