package com.dishut_lampung.sitanihut.presentation.penyuluh.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.usecase.penyuluh.GetPenyuluhDetailUseCase
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PenyuluhDetailUiState(
    val isLoading: Boolean = false,
    val penyuluh: Penyuluh? = null,
    val error: String? = null
)

@HiltViewModel
class PenyuluhDetailViewModel @Inject constructor(
    private val getPenyuluhDetailUseCase: GetPenyuluhDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PenyuluhDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val penyuluhId: String = checkNotNull(savedStateHandle["id"])

    init {
        getDetail()
    }

    fun onRetry() {
        getDetail()
    }

    private fun getDetail() {
        viewModelScope.launch {
            getPenyuluhDetailUseCase(penyuluhId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                penyuluh = result.data,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }
}