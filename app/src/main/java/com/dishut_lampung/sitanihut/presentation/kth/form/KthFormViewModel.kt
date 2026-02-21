package com.dishut_lampung.sitanihut.presentation.kth.form

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.usecase.kph.GetKphListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.CreateKthUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.UpdateKthUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.ValidateKthInputUseCase
import com.dishut_lampung.sitanihut.domain.usecase.profile.GetMyProfileUseCase
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
import com.dishut_lampung.sitanihut.util.ConnectivityObserver
import com.dishut_lampung.sitanihut.util.Resource
import com.dishut_lampung.sitanihut.util.WilayahLampungData
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
class KthFormViewModel @Inject constructor(
    private val createKthUseCase: CreateKthUseCase,
    private val getKthDetailUseCase: GetKthDetailUseCase,
    private val updateKthUseCase: UpdateKthUseCase,
    private val validateKthInputUseCase: ValidateKthInputUseCase,
    private val getKphListUseCase: GetKphListUseCase,
    private val connectivityObserver: ConnectivityObserver,
    private val savedStateHandle: SavedStateHandle,
    private val userPreferences: UserPreferences,
    private val getMyProfileUseCase: GetMyProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(KthFormUiState())
    val uiState = _uiState.asStateFlow()

    private val currentKthId: String? = savedStateHandle.get<String>("id")

    init {
        observeConnectivity()
        loadKphOptions()
        loadKabupatenOptions()

        if (currentKthId != null) {
            loadExistingKthData(currentKthId)
        }else {
            setDefaultKphFromUser()
        }
    }

    fun onEvent(event: KthFormUiEvent) {
        when (event) {
            is KthFormUiEvent.OnNameChange -> {
                _uiState.update { it.copy(name = event.value, nameError = null) }
            }
            is KthFormUiEvent.OnKabupatenSearchTextChange -> {
                _uiState.update { it.copy(selectedKabupaten = event.text) }
            }
            is KthFormUiEvent.OnKecamatanSearchTextChange -> {
                _uiState.update { it.copy(selectedKecamatan = event.text) }
            }
            is KthFormUiEvent.OnDesaSearchTextChange -> {
                _uiState.update { it.copy(selectedDesa = event.text) }
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
                _uiState.update { it.copy(whatsappNumber = event.value) }

                val phoneRegex = Regex("^(08|\\+628)[0-9]*$")
                var errorMsg: String? = null

                if (event.value.isNotEmpty()) {
                    if (!event.value.matches(phoneRegex)) {
                        errorMsg = "Masukkan nomor valid (08.. atau +628..) tanpa spasi"
                    } else if (event.value.length > 14) {
                        errorMsg = "Nomor telepon maksimal 14 digit"
                    }else if (event.value.length < 10) {
                        errorMsg = "Nomor telepon minimal 10 digit"
                    }
                }
                _uiState.update { it.copy(whatsappError = errorMsg) }
            }
            is KthFormUiEvent.OnKphSearchTextChange -> {
                _uiState.update { state ->
                    val newText = event.text
                    if (newText == state.selectedKphName && state.selectedKphId.isNotEmpty()) {
                        return@update state
                    }
                    val matchingOption = state.kphOptions.find {
                        it.name.equals(newText, ignoreCase = true)
                    }

                    state.copy(
                        selectedKphName = newText,
                        selectedKphId = matchingOption?.id ?: ""
                    )
                }
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

    private fun observeConnectivity() {
        connectivityObserver.observe()
            .onEach { status ->
                _uiState.update {
                    it.copy(isOnline = status == ConnectivityObserver.Status.Available)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadKphOptions() {
        getKphListUseCase().onEach { kphList ->
//            Log.d("DEBUG_KPH", "Data KPH dari DB: ${kphList.size} item")
            _uiState.update { it.copy(kphOptions = kphList) }
        }.launchIn(viewModelScope)
    }

    private fun loadKabupatenOptions() {
        val kabupatenList = WilayahLampungData.getKabupatenList()

        _uiState.update {
            it.copy(kabupatenOptions = kabupatenList)
        }
    }

    private fun loadExistingKthData(id: String) {
        getKthDetailUseCase(id).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    val data = result.data
                    if (data != null) {
                        val kecamatanList = WilayahLampungData.getKecamatanByKabupaten(data.kabupaten)
                        val desaList = WilayahLampungData.getDesaByKecamatan(data.kabupaten, data.kecamatan ?: "")

                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isEditMode = true,
                                name = data.name,
                                coordinator = data.coordinator ?: "",
                                whatsappNumber = data.whatsappNumber ?: "",
                                selectedKabupaten = data.kabupaten,
                                kecamatanOptions = kecamatanList,
                                selectedKecamatan = data.kecamatan ?: "",
                                desaOptions = desaList,
                                selectedDesa = data.desa,
                                selectedKphId = data.kphId ?: "",
                                selectedKphName = data.kphName ?: ""
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
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
        if (!_uiState.value.isOnline) {
            _uiState.update { it.copy(error = "Tidak ada koneksi internet") }
            return
        }

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

            val result = if (currentKthId == null) {
                createKthUseCase(input)
            } else {
                updateKthUseCase(currentKthId, input)
            }

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

    private fun setDefaultKphFromUser() {
        viewModelScope.launch {
            val currentUserId = userPreferences.userId.first()
            if (!currentUserId.isNullOrEmpty()) {
                getMyProfileUseCase(currentUserId).onEach { result ->
                    if (result is Resource.Success) {
                        val user = result.data
                        if (user != null && user.kphId != null && user.kphName != null) {
                            _uiState.update {
                                it.copy(
                                    selectedKphId = user.kphId,
                                    selectedKphName = user.kphName
                                )
                            }
                        }
                    }
                }.launchIn(this)
            }
        }
    }
}