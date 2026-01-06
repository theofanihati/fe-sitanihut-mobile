package com.dishut_lampung.sitanihut.presentation.kth.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.usecase.kph.GetKphListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.CreateKthUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.ValidateKthInputUseCase
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.util.Resource
import com.dishut_lampung.sitanihut.util.WilayahLampungData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KthFormViewModel @Inject constructor(
    private val createKthUseCase: CreateKthUseCase,
    private val validateKthInputUseCase: ValidateKthInputUseCase,
    private val getKphListUseCase: GetKphListUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(KthFormUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadKphOptions()
    }

    fun onEvent(event: KthFormUiEvent) {
        TODO()
    }

    private fun loadKphOptions() {
        TODO()
    }
}