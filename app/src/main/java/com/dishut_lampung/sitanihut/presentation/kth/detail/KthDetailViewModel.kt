package com.dishut_lampung.sitanihut.presentation.kth.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KthDetailViewModel  @Inject constructor(
    private val getKthDetailUseCase: GetKthDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(KthDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val kthId: String = checkNotNull(savedStateHandle["kthId"])

    init {
        TODO()
    }

    fun onRetry() {
        TODO()
    }
}