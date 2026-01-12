package com.dishut_lampung.sitanihut.presentation.petani.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.usecase.petani.GetPetaniDetailUseCase
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetaniDetailViewModel @Inject constructor(
    private val getPetaniDetailUseCase: GetPetaniDetailUseCase,
    private val userPreferences: UserPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(PetaniDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val kthId: String = checkNotNull(savedStateHandle["id"])

    init {
        TODO()
    }

    fun onRetry() {
       TODO()
    }
}