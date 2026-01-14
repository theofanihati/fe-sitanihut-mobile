package com.dishut_lampung.sitanihut.presentation.petani.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.usecase.kph.GetKphListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.CreatePetaniUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.GetPetaniDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.UpdatePetaniUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.ValidatePetaniInputUseCase
import com.dishut_lampung.sitanihut.domain.usecase.profile.GetUserDetailUseCase
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetaniFormViewModel @Inject constructor(
    private val createPetaniUseCase: CreatePetaniUseCase,
    private val getPetaniDetailUseCase: GetPetaniDetailUseCase,
    private val updatePetaniUseCase: UpdatePetaniUseCase,
    private val validatePetaniInputUseCase: ValidatePetaniInputUseCase,
    private val getKphListUseCase: GetKphListUseCase,
    private val getKthListUseCase: GetKthListUseCase,
    private val userPreferences: UserPreferences,
    private val getUserDetailUseCase: GetUserDetailUseCase,
    private val connectivityObserver: ConnectivityObserver,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetaniFormUiState())
    val uiState = _uiState.asStateFlow()

    private val currentPetaniId: String? = savedStateHandle.get<String>("id")
    private var allKthInKph: List<Kth> = emptyList()

    init {}

    fun onEvent(event: PetaniFormEvent) {
        TODO()
    }
}