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

    private val currentKthId: String? = savedStateHandle.get<String>("kthId")

    init {
        loadKphOptions()
    }

    fun onEvent(event: KthFormUiEvent) {
        when (event) {
            is KthFormUiEvent.OnNameChange -> {
                _uiState.update { it.copy(name = event.value, nameError = null) }
            }
            is KthFormUiEvent.OnKabupatenSelected -> {
                updateKabupaten(event.value)
            }
            is KthFormUiEvent.OnKecamatanSelected -> {
                updateKecamatan(event.value)
            }
            is KthFormUiEvent.OnDesaSelected -> {
                _uiState.update { it.copy(selectedDesa = event.value, desaError = null) }
            }
            is KthFormUiEvent.OnCoordinatorChange -> {
                _uiState.update { it.copy(coordinator = event.value, coordinatorError = null) }
            }
            is KthFormUiEvent.OnWhatsappChange -> {
                _uiState.update { it.copy(whatsappNumber = event.value, whatsappError = null) }
            }
            is KthFormUiEvent.OnKphSelected -> {
                _uiState.update {
                    it.copy(
                        selectedKphId = event.kph.id,
                        selectedKphName = event.kph.name,
                        kphError = null
                    )
                }
            }
            is KthFormUiEvent.OnShowConfirmDialog -> {
                _uiState.update { it.copy(showConfirmDialog = true) }
            }
            is KthFormUiEvent.OnDismissConfirmDialog -> {
                _uiState.update { it.copy(showConfirmDialog = false) }
            }
            is KthFormUiEvent.OnSubmit -> {
                submitKth()
            }
            is KthFormUiEvent.OnDismissMessage -> {
                _uiState.update { it.copy(error = null, successMessage = null) }
            }
            is KthFormUiEvent.OnShowUserMessage -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(successMessage = null, error = null) }
                   if (event.type == MessageType.Success) {
                        _uiState.update { it.copy(successMessage = event.message) }
                    } else {
                        _uiState.update { it.copy(error = event.message) }
                    }
                }
            }
        }
    }

    private fun loadKphOptions() {
        getKphListUseCase().onEach { kphList ->
            _uiState.update { it.copy(kphOptions = kphList) }
        }.launchIn(viewModelScope)
    }

    private fun updateKabupaten(kabupaten: String) {
        val kecamatanList = WilayahLampungData.getKecamatanByKabupaten(kabupaten)
        _uiState.update {
            it.copy(
                selectedKabupaten = kabupaten,
                kabupatenError = null,
                kecamatanOptions = kecamatanList,
                selectedKecamatan = "",
                desaOptions = emptyList(),
                selectedDesa = ""
            )
        }
    }

    private fun updateKecamatan(kecamatan: String) {
        val desaList = WilayahLampungData.getDesaByKecamatan(_uiState.value.selectedKabupaten, kecamatan)
        _uiState.update {
            it.copy(
                selectedKecamatan = kecamatan,
                kecamatanError = null,
                desaOptions = desaList,
                selectedDesa = ""
            )
        }
    }

    private fun submitKth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val input = CreateKthInput(
                name = _uiState.value.name,
                desa = _uiState.value.selectedDesa,
                kecamatan = _uiState.value.selectedKecamatan,
                kabupaten = _uiState.value.selectedKabupaten,
                coordinator = _uiState.value.coordinator,
                whatsappNumber = _uiState.value.whatsappNumber,
                kphId = _uiState.value.selectedKphId,
                kphName = _uiState.value.selectedKphName
            )

            val validationResult = validateKthInputUseCase.execute(input)

            if (!validationResult.successful) {
                val errors = validationResult.fieldErrors
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = validationResult.errorMessage,
                        showConfirmDialog = false,
                        nameError = errors["name"],
                        kabupatenError = errors["kabupaten"],
                        kecamatanError = errors["kecamatan"],
                        desaError = errors["desa"],
                        coordinatorError = errors["coordinator"],
                        whatsappError = errors["whatsappNumber"],
                        kphError = errors["kphId"] ?: errors["kph_name"]
                    )
                }
                return@launch
            }

            val result = createKthUseCase(input)

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Berhasil disimpan!",
                            showConfirmDialog = false,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                            showConfirmDialog = false
                        )
                    }
                }
                else -> {}
            }
        }
    }
}