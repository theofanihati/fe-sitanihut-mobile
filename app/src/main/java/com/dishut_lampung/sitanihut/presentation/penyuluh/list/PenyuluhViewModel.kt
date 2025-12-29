package com.dishut_lampung.sitanihut.presentation.penyuluh.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.GetPenyuluhUseCase
import com.dishut_lampung.sitanihut.presentation.penyuluh.PenyuluhEvent
import com.dishut_lampung.sitanihut.presentation.penyuluh.PenyuluhUiState
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PenyuluhViewModel @Inject constructor(
    private val getPenyuluhUseCase: GetPenyuluhUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PenyuluhUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
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
            PenyuluhEvent.OnRefresh -> getPenyuluh(searchQuery = _uiState.value.searchQuery, isRefresh = true)
            PenyuluhEvent.OnDismissError -> _uiState.update { it.copy(error = null) }
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
                    error = null
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
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }
}