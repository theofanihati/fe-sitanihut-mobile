package com.dishut_lampung.sitanihut.presentation.penyuluh

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.GetPenyuluhUseCase
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        fetchPenyuluh()
    }

    fun onEvent(event: PenyuluhEvent) {
        when (event) {
            PenyuluhEvent.OnRefresh -> fetchPenyuluh(isRefresh = true)
            PenyuluhEvent.OnDismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun fetchPenyuluh(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val role = userPreferences.userRole.first() ?: ""

            _uiState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    error = null
                )
            }

            getPenyuluhUseCase(role).collect { result ->
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