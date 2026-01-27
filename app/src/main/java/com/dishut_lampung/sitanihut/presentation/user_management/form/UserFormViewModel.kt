package com.dishut_lampung.sitanihut.presentation.user_management.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.model.Role
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.usecase.kph.GetKphListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.domain.usecase.profile.GetMyProfileUseCase
import com.dishut_lampung.sitanihut.domain.usecase.role.GetRolesUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.CreateUserUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.GetUserDetailUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.UpdateUserUseCase
import com.dishut_lampung.sitanihut.domain.usecase.user_management.ValidateUserManagementInputUseCase
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
class UserFormViewModel  @Inject constructor(
    private val createUserUseCase: CreateUserUseCase,
    private val getUserDetailUseCase: GetUserDetailUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val validateUserManagementInputUseCase: ValidateUserManagementInputUseCase,
    private val getKphListUseCase: GetKphListUseCase,
    private val getKthListUseCase: GetKthListUseCase,
    private val getRolesUseCase: GetRolesUseCase,
    private val userPreferences: UserPreferences,
    private val connectivityObserver: ConnectivityObserver,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserFormUiState())
    val uiState = _uiState.asStateFlow()

    private val currentUserId: String? = savedStateHandle.get<String>("id")
    private var allKthInKph: List<Kth> = emptyList()
    private var originalData: UserDetail? = null
    private var availableRoles: List<Role> = emptyList()
    private var allKphOptions: List<Kph> = emptyList()

    private val phoneRegex = Regex("^(08[0-9]{8,14}|\\+628[0-9]{8,14})$")
    private val nikRegex = Regex("^[0-9]{16}$")
    private val nipRegex = Regex("^[0-9]{18}$")
    private val emailRegex = Regex("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+")

    private val hasLowerCase = Regex("[a-z]")
    private val hasUpperCase = Regex("[A-Z]")
    private val hasDigit = Regex("\\d")
    private val hasSpecialChar = Regex("[!@#\$%^&*(),.?\":{}|<>]")

    init {
        observeConnectivity()
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val role = userPreferences.userRole.first() ?: ""
            getRolesUseCase().collect { result ->
                if (result is Resource.Success) {
                    availableRoles = result.data ?: emptyList()
                    val petaniRole = availableRoles.find { it.name.equals("petani", ignoreCase = true) }
                    _uiState.update {
                        it.copy(
                            roleId = petaniRole?.id ?: "",
                            roleName = "petani"
                        )
                    }
                }
            }

            getKphListUseCase().onEach { kphList ->
                allKphOptions = kphList
                _uiState.update { it.copy(kphOptions = kphList)
                }
            }.launchIn(this)

            if (currentUserId != null) {
                loadExistingUserData(currentUserId, role)
            }
        }
    }

    fun onEvent(event: UserFormEvent) {
        when (event) {
//            is UserFormEvent.OnEmailChange -> _uiState.update { it.copy(email = event.value, emailError = null) }
//            is UserFormEvent.OnPasswordChange -> _uiState.update { it.copy(password = event.value, passwordError = null) }
//            is UserFormEvent.OnConfirmPasswordChange -> _uiState.update { it.copy(confirmPassword = event.value, confirmPasswordError = null) }
            is UserFormEvent.OnEmailChange -> {
                _uiState.update { it.copy(email = event.value) }
                val errorMsg = when {
                    event.value.isEmpty() -> "Email tidak boleh kosong"
                    !event.value.matches(emailRegex) -> "Format email tidak valid"
                    else -> null
                }
                _uiState.update { it.copy(emailError = errorMsg) }
            }
            is UserFormEvent.OnPasswordChange -> {
                _uiState.update { it.copy(password = event.value) }
                val password = event.value
                val isEditMode = _uiState.value.isEditMode

                val errorMsg = if (isEditMode && password.isEmpty()) {
                    null
                } else if (password.length < 8) {
                    "Password minimal 8 karakter"
                } else if (!password.contains(hasLowerCase)) {
                    "Password harus memiliki setidaknya satu huruf kecil"
                } else if (!password.contains(hasUpperCase)) {
                    "Password harus memiliki setidaknya satu huruf besar (kapital)"
                } else if (!password.contains(hasDigit)) {
                    "Password harus memiliki setidaknya satu angka"
                } else if (!password.contains(hasSpecialChar)) {
                    "Password harus memiliki setidaknya satu karakter spesial (!@#$...)"
                } else {
                    null
                }

                _uiState.update {
                    it.copy(
                        passwordError = errorMsg,
                        confirmPasswordError = if (it.confirmPassword.isNotEmpty() && password != it.confirmPassword) {
                            "Password dan konfirmasi password tidak cocok"
                        } else null
                    )
                }
            }

            is UserFormEvent.OnConfirmPasswordChange -> {
                _uiState.update { it.copy(confirmPassword = event.value) }
                val errorMsg = if (event.value != _uiState.value.password) {
                    "Password dan konfirmasi password tidak cocok"
                } else {
                    null
                }
                _uiState.update { it.copy(confirmPasswordError = errorMsg) }
            }
            is UserFormEvent.OnNameChange -> {
                _uiState.update { it.copy(
                    name = event.value,
                    nameError = if (event.value.isBlank()) "Nama lengkap wajib diisi" else null
                ) }
            }

            is UserFormEvent.OnIdentityNumberChange -> {
                if (event.value.all { it.isDigit() } && event.value.length <= 18) {
                    val isPetani = _uiState.value.roleName.equals("petani", ignoreCase = true)
                    val label = if (isPetani) "NIK" else "NIP"
                    val lengthRequirement = if (isPetani) 16 else 18

                    var errorMsg: String? = null
                    if (event.value.isBlank()) {
                        errorMsg = "$label wajib diisi"
                    } else if (event.value.length != lengthRequirement) {
                        errorMsg = "$label harus terdiri dari $lengthRequirement digit"
                    }

                    _uiState.update {
                        it.copy(
                            identityNumber = event.value,
                            identityNumberError = errorMsg
                        )
                    }
                }
                }


            is UserFormEvent.OnGenderChange -> {
                _uiState.update { it.copy(
                    gender = event.value,
                    genderError = if (event.value.isBlank()) "Jenis kelamin wajib dipilih" else null
                ) }
            }

            is UserFormEvent.OnAddressChange -> {
                _uiState.update { it.copy(
                    address = event.value,
                    addressError = if (event.value.isBlank()) "Alamat wajib diisi" else null
                ) }
            }

            is UserFormEvent.OnWhatsAppChange -> {
                _uiState.update { it.copy(whatsAppNumber = event.value) }
                var errorMsg: String? = null

                if (event.value.isNotEmpty()) {
                    if(event.value.isEmpty()){
                        errorMsg = "Nomor telepon tidak boleh kosong"
                    } else if (!event.value.matches(phoneRegex)) {
                        errorMsg = "Masukkan nomor valid (08.. atau +628..) tanpa spasi"
                    } else if (event.value.length > 14) {
                        errorMsg = "Nomor telepon maksimal 14 digit"
                    } else if (event.value.length < 10) {
                        errorMsg = "Nomor telepon minimal 10 digit"
                    }
                }
                _uiState.update { it.copy(whatsAppNumberError = errorMsg) }
            }

            is UserFormEvent.OnLastEducationChange -> {
                _uiState.update { it.copy(
                    lastEducation = event.value,
                    lastEducationError = if (event.value.isBlank()) "Pendidikan terakhir wajib dipilih" else null
                ) }
            }

            is UserFormEvent.OnSideJobChange -> {
                _uiState.update { it.copy(
                    sideJob = event.value,
                    sideJobError = if (event.value.isBlank()) "Pekerjaan sampingan wajib diisi" else null
                ) }
            }

            is UserFormEvent.OnPositionChange -> { _uiState.update { it.copy(position = event.value, positionError = null) } }

            is UserFormEvent.OnLandAreaChange -> {
                val isNumberOrDot = event.value.all { it.isDigit() || it == '.' }
                val dotCount = event.value.count { it == '.' }

                if (isNumberOrDot && dotCount <= 1) {
                    val num = event.value.toDoubleOrNull()
                    val errorMsg = when {
                        event.value.isBlank() -> "Luas lahan wajib diisi"
                        num == null || num <= 0 -> "Luas lahan harus diisi angka lebih dari 0"
                        else -> null
                    }
                    _uiState.update { it.copy(landArea = event.value, landAreaError = errorMsg) }
                }
            }

            is UserFormEvent.OnKphSearchTextChange -> {
                _uiState.update { state ->
                    val newText = event.text
                    val matchingOption = state.kphOptions.find {
                        it.name.equals(newText, ignoreCase = true)
                    }

                    val filteredOptions = if (newText.isBlank()) {
                        allKphOptions
                    } else {
                        allKphOptions.filter { it.name.contains(newText, ignoreCase = true) }
                    }

                    state.copy(
                        selectedKphName = newText,
                        selectedKphId = matchingOption?.id ?: "",
                        kphOptions = filteredOptions
                    )
                }
            }

            is UserFormEvent.OnKphSelected -> {
                _uiState.update {
                    it.copy(
                        selectedKphId = event.kph.id,
                        selectedKphName = event.kph.name,
                        kphError = null,
                        selectedKthId = "",
                        selectedKthName = "",
                        kthError = "Asal KTH tidak boleh kosong"
                    )
                }
                viewModelScope.launch {
                    val role = userPreferences.userRole.first() ?: ""
                    loadKthOptions(role, event.kph.id)
                }
            }

            is UserFormEvent.OnKthSearchTextChange -> {
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

            is UserFormEvent.OnKthSelected -> {
                _uiState.update {
                    it.copy(
                        selectedKthId = event.kth.id,
                        selectedKthName = event.kth.name,
                        kthError = null,
                        kthOptions = allKthInKph
                    )
                }
            }

            is UserFormEvent.OnTogglePasswordVisibility -> _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            is UserFormEvent.OnToggleConfirmPasswordVisibility -> _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            is UserFormEvent.OnSubmit -> submit()
            is UserFormEvent.OnShowConfirmDialog -> { _uiState.update { it.copy(showConfirmDialog = true) } }
            is UserFormEvent.OnDismissConfirmDialog -> { _uiState.update { it.copy(showConfirmDialog = false) } }
            is UserFormEvent.OnDismissMessage -> { _uiState.update { it.copy(error = null, successMessage = null) } }

            is UserFormEvent.OnShowUserMessage -> {
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

    private fun loadExistingUserData(id: String, role: String) {
        getUserDetailUseCase(id).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    val data = result.data
//                    android.util.Log.d("DEBUG_USER", "=== CEK DATA MASUK VIEWMODEL ===")
//                    android.util.Log.d("DEBUG_USER", "Nama: ${data?.name}")
//                    android.util.Log.d("DEBUG_USER", "KPH ID: '${data?.kphId}' (Harus ada isinya)")
//                    android.util.Log.d("DEBUG_USER", "KTH ID: '${data?.kthId}' (Harus ada isinya)")
                    originalData = data
                    if (data != null) {
                        val roleId = availableRoles.find { it.name.equals(data.role, ignoreCase = true) }?.id ?: ""
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isEditMode = true,
                                password = "",
                                confirmPassword = "",
                                email = data.email ?: "",
                                roleName = data.role ?: "petani",
                                roleId = roleId,
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
            val state = _uiState.value

            val targetRoleId = state.roleId.ifBlank {
                availableRoles.find { it.name.equals("petani", ignoreCase = true) }?.id ?: ""
            }

            if (currentUserId == null) {
                val input = CreateUserInput(
                    isEditMode = currentUserId != null,
                    email = state.email,
                    password = state.password,
                    confirmPassword = state.confirmPassword,
                    role = "petani",
                    roleId = targetRoleId,
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

                val validationResult = validateUserManagementInputUseCase.execute(input)

                if (!validationResult.successful) {
                    val errors = validationResult.fieldErrors
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = validationResult.errorMessage,
                            showConfirmDialog = false,
                            emailError = errors["email"],
                            passwordError = errors["password"],
                            confirmPasswordError = errors["confirmPassword"],
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
                val result = createUserUseCase(input)
                handleResult(result)
            } else {
                val changes = mutableMapOf<String, Any?>()
                val current = _uiState.value
                val old = originalData

                if (old != null) {
                    if (state.email != old.email) changes["email"] = state.email
                    if (state.password.isNotEmpty()) {
                        changes["password"] = state.password
                    }

                    if (current.name != old.name) changes["nama_user"] = current.name

                    if (current.identityNumber != old.identityNumber) changes["nomor_induk"] = current.identityNumber

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

                val result = updateUserUseCase(currentUserId, changes)
                handleResult(result)
            }
        }
    }

    private fun <T> handleResult(result: Resource<T>) {
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