package com.dishut_lampung.sitanihut.presentation.petani.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.usecase.kph.GetKphListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.CreatePetaniUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.GetPetaniDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.UpdatePetaniUseCase
import com.dishut_lampung.sitanihut.domain.usecase.petani.ValidatePetaniInputUseCase
import com.dishut_lampung.sitanihut.domain.usecase.profile.GetMyProfileUseCase
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType
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
    private val getUserDetailUseCase: GetMyProfileUseCase,
    private val connectivityObserver: ConnectivityObserver,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetaniFormUiState())
    val uiState = _uiState.asStateFlow()

    private val currentPetaniId: String? = savedStateHandle.get<String>("id")
    private var allKthInKph: List<Kth> = emptyList()
    private var originalData: Petani? = null

    init {
        observeConnectivity()

        viewModelScope.launch {
            val userId = userPreferences.userId.first() ?: ""
            val role = userPreferences.userRole.first() ?: ""
            var userKphId = ""
            var userKphName = ""

            if (userId.isNotEmpty()) {
                val userResult = getUserDetailUseCase(userId).first()
                if (userResult is Resource.Success) {
                    val user = userResult.data
                    userKphId = user?.kphId ?: ""
                    userKphName = user?.kphName ?: ""
                }
            }
            _uiState.update {
                it.copy(
                    selectedKphId = userKphId,
                    selectedKphName = userKphName
                )
            }
            loadKthOptions(role, userKphId)
            loadKphOptions()

            if (currentPetaniId != null) {
                loadExistingPetaniData(currentPetaniId, role)
            }
        }
    }

    fun onEvent(event: PetaniFormEvent) {
        when (event) {
            is PetaniFormEvent.OnNameChange -> {
                _uiState.update { it.copy(name = event.value, nameError = null) }
            }

            is PetaniFormEvent.OnIdentityNumberChange -> {
                if (event.value.all { it.isDigit() } && event.value.length <= 16) {
                    _uiState.update {
                        it.copy(
                            identityNumber = event.value,
                            identityNumberError = null
                        )
                    }
                }
            }

            is PetaniFormEvent.OnGenderChange -> {
                _uiState.update { it.copy(gender = event.value, genderError = null) }
            }

            is PetaniFormEvent.OnAddressChange -> {
                _uiState.update { it.copy(address = event.value, addressError = null) }
            }

            is PetaniFormEvent.OnWhatsAppChange -> {
                _uiState.update { it.copy(whatsAppNumber = event.value) }

                val phoneRegex = Regex("^(08|\\+628)[0-9]*$")
                var errorMsg: String? = null

                if (event.value.isNotEmpty()) {
                    if (!event.value.matches(phoneRegex)) {
                        errorMsg = "Masukkan nomor valid (08.. atau +628..) tanpa spasi"
                    } else if (event.value.length > 14) {
                        errorMsg = "Nomor telepon maksimal 14 digit"
                    } else if (event.value.length < 10) {
                        errorMsg = "Nomor telepon minimal 10 digit"
                    }
                }
                _uiState.update { it.copy(whatsAppNumberError = errorMsg) }
            }

            is PetaniFormEvent.OnLastEducationChange -> {
                _uiState.update { it.copy(lastEducation = event.value, lastEducationError = null) }
            }

            is PetaniFormEvent.OnSideJobChange -> {
                _uiState.update { it.copy(sideJob = event.value, sideJobError = null) }
            }

            is PetaniFormEvent.OnLandAreaChange -> {
                val isNumberOrDot = event.value.all { it.isDigit() || it == '.' }
                val dotCount = event.value.count { it == '.' }

                if (isNumberOrDot && dotCount <= 1) {
                    _uiState.update { it.copy(landArea = event.value, landAreaError = null) }
                }
            }

            is PetaniFormEvent.OnKphSearchTextChange -> {
                _uiState.update { state ->
                    val newText = event.text
                    val matchingOption = state.kphOptions.find {
                        it.name.equals(newText, ignoreCase = true)
                    }

                    val filteredOptions = if (newText.isBlank()) {
                        allKthInKph
                    } else {
                        allKthInKph.filter { it.name.contains(newText, ignoreCase = true) }
                    }

                    state.copy(
                        selectedKphName = newText,
                        selectedKphId = matchingOption?.id ?: "",
                        kthOptions = filteredOptions
                    )
                }
            }

            is PetaniFormEvent.OnKphSelected -> {
                _uiState.update {
                    it.copy(
                        selectedKphId = event.kph.id,
                        selectedKphName = event.kph.name,
                        kphError = null,
                        selectedKthId = "",
                        selectedKthName = ""
                    )
                }
                viewModelScope.launch {
                    val role = userPreferences.userRole.first() ?: ""
                    loadKthOptions(role, event.kph.id)
                }
            }

            is PetaniFormEvent.OnKthSearchTextChange -> {
                _uiState.update { state ->
                    val newText = event.text

                    val matchingOption = allKthInKph.find {
                        it.name.equals(newText, ignoreCase = true)
                    }

                    val filteredOptions = if (newText.isBlank()) {
                        allKthInKph
                    } else {
                        allKthInKph.filter { it.name.contains(newText, ignoreCase = true) }
                    }

                    state.copy(
                        selectedKthName = newText,
                        selectedKthId = matchingOption?.id ?: "",
                        kthOptions = filteredOptions
                    )
                }
            }

            is PetaniFormEvent.OnKthSelected -> {
                _uiState.update {
                    it.copy(
                        selectedKthId = event.kth.id,
                        selectedKthName = event.kth.name,
                        kthError = null,
                        kthOptions = allKthInKph
                    )
                }
            }

            is PetaniFormEvent.OnShowConfirmDialog -> {
                _uiState.update { it.copy(showConfirmDialog = true) }
            }

            is PetaniFormEvent.OnDismissConfirmDialog -> {
                _uiState.update { it.copy(showConfirmDialog = false) }
            }

            is PetaniFormEvent.OnSubmit -> {
                submit()
            }

            is PetaniFormEvent.OnDismissMessage -> {
                _uiState.update { it.copy(error = null, successMessage = null) }
            }

            is PetaniFormEvent.OnShowUserMessage -> {
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

    private fun loadKthOptions(role: String, kphId: String) {
        getKthListUseCase(role = role, query = "")
            .onEach { result ->
                if (result is Resource.Success) {
                    val fullList = result.data ?: emptyList()
                    if (fullList.isNotEmpty()) {
                        val sample = fullList[0]
//                        android.util.Log.d("DEBUG_KTH", "--- CEK DATA ---")
//                        android.util.Log.d("DEBUG_KTH", "1. User KPH ID (Target): '$kphId'")
//                        android.util.Log.d("DEBUG_KTH", "2. Data API KPH ID: '${sample.kphId}'")
//                        android.util.Log.d("DEBUG_KTH", "3. Data API Nama: '${sample.name}'")
                    } else {
//                        android.util.Log.d("DEBUG_KTH", "List DB Kosong!")
                    }
                    val filteredKth = fullList.filter { it.kphId == kphId }

                    allKthInKph = filteredKth
                    _uiState.update { it.copy(kthOptions = filteredKth) }
                }
            }.launchIn(viewModelScope)
    }

    private fun loadExistingPetaniData(id: String, role: String) {
        getPetaniDetailUseCase(id).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    val data = result.data
//                    android.util.Log.d("DEBUG_PETANI", "=== CEK DATA MASUK VIEWMODEL ===")
//                    android.util.Log.d("DEBUG_PETANI", "Nama: ${data?.name}")
//                    android.util.Log.d("DEBUG_PETANI", "KPH ID: '${data?.kphId}' (Harus ada isinya)")
//                    android.util.Log.d("DEBUG_PETANI", "KTH ID: '${data?.kthId}' (Harus ada isinya)")
                    originalData = data
                    if (data != null) {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isEditMode = true,
                                name = data.name,
                                identityNumber = data.identityNumber ?: "",
                                gender = data.gender ?: "",
                                address = data.address ?: "",
                                whatsAppNumber = data.whatsAppNumber ?: "",
                                lastEducation = data.lastEducation ?: "",
                                sideJob = data.sideJob ?: "",
                                landArea = data.landArea?.toString()?.removeSuffix(".0") ?: "",
                                selectedKphId = data.kphId ?: "",
                                selectedKphName = data.kphName ?: "",
                                selectedKthId = data.kthId ?: "",
                                selectedKthName = data.kthName ?: "",
                            )
                        }
                        if (!data.kphId.isNullOrBlank()) {
                            loadKthOptions(role, data.kphId)
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

    private fun submit() {
        if (!_uiState.value.isOnline) {
            _uiState.update { it.copy(error = "Tidak ada koneksi internet") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            if (currentPetaniId == null) {
                val input = CreatePetaniInput(
                    name = _uiState.value.name,
                    identityNumber = _uiState.value.identityNumber,
                    gender = _uiState.value.gender,
                    address = _uiState.value.address,
                    whatsAppNumber = _uiState.value.whatsAppNumber,
                    lastEducation = _uiState.value.lastEducation,
                    sideJob = _uiState.value.sideJob,
                    landArea = _uiState.value.landArea,
                    kphId = _uiState.value.selectedKphId,
                    kthId = _uiState.value.selectedKthId,
                )

                val validationResult = validatePetaniInputUseCase.execute(input)

                if (!validationResult.successful) {
                    val errors = validationResult.fieldErrors
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = validationResult.errorMessage,
                            showConfirmDialog = false,
                            nameError = errors["name"],
                            identityNumberError = errors["identityNumber"],
                            genderError = errors["gender"],
                            addressError = errors["address"],
                            whatsAppNumberError = errors["whatsAppNumber"],
                            lastEducationError = errors["lastEducation"],
                            sideJobError = errors["sideJob"],
                            landAreaError = errors["landArea"],
                            kphError = errors["kphId"] ?: errors["kph_name"],
                            kthError = errors["kthId"] ?: errors["kth_name"]
                        )
                    }
                    return@launch
                }
            val result = createPetaniUseCase(input)
            handleResult(result)
            } else {
                val changes = mutableMapOf<String, Any?>()
                val current = _uiState.value
                val old = originalData

                if (old != null) {
                    if (current.name != old.name) changes["nama_petani"] = current.name

                    if (current.identityNumber != old.identityNumber) changes["nik"] = current.identityNumber

                    if (current.gender != old.gender) changes["jenis_kelamin"] = current.gender
                    if (current.address != old.address) changes["alamat"] = current.address
                    if (current.whatsAppNumber != old.whatsAppNumber) changes["nomor_wa"] = current.whatsAppNumber
                    if (current.lastEducation != old.lastEducation) changes["pendidikan_terakhir"] = current.lastEducation
                    if (current.sideJob != old.sideJob) changes["pekerjaan_sampingan"] = current.sideJob

                    val newLandArea = current.landArea.toDoubleOrNull()
                    if (newLandArea != old.landArea) {
                        changes["luas_lahan"] = newLandArea
                    }

                    if (current.selectedKphId != old.kphId) changes["id_kph"] = current.selectedKphId
                    if (current.selectedKthId != old.kthId) changes["id_kth"] = current.selectedKthId
                }

                if (changes.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Tidak ada perubahan data") }
                    return@launch
                }

                val result = updatePetaniUseCase(currentPetaniId, changes)
                handleResult(result)
            }
        }
    }

    private fun <T> handleResult(result: Resource<T>) { // Tambahkan <T>
        when (result) {
            is Resource.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Berhasil disimpan!",
                        showConfirmDialog = false
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