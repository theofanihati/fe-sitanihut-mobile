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
        TODO()
    }

    private fun fetchPenyuluh(isRefresh: Boolean = false) {
        TODO()
    }
}