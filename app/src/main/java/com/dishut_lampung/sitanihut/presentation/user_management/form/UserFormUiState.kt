package com.dishut_lampung.sitanihut.presentation.user_management.form

import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType

data class UserFormUiState (
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isFormValid: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val isEditMode: Boolean = false,
    val isOnline: Boolean = true,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,

    val roleId: String = "",
    val roleName: String = "",

    val email:String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,

    val name: String = "",
    val nameError: String? = null,
    val identityNumber: String = "",
    val identityNumberError: String? = null,
    val gender: String = "",
    val genderError: String? = null,
    val address: String = "",
    val addressError: String? = null,
    val whatsAppNumber: String = "",
    val whatsAppNumberError: String? = null,
    val lastEducation: String = "",
    val lastEducationError: String? = null,
    val sideJob: String = "",
    val sideJobError: String? = null,
    val landArea: String = "",
    val landAreaError: String? = null,
    val position: String = "",
    val positionError: String? = null,

    val kphOptions: List<Kph> = emptyList(),
    val selectedKphId: String = "",
    val selectedKphName: String = "",
    val kphError: String? = null,

    val kthOptions: List<Kth> = emptyList(),
    val selectedKthId: String = "",
    val selectedKthName: String = "",
    val kthError: String? = null,
)

sealed class UserFormEvent{
    data class OnEmailChange(val value: String) : UserFormEvent()
    data class OnPasswordChange(val value: String) : UserFormEvent()
    data class OnConfirmPasswordChange(val value: String) : UserFormEvent()
    data class OnNameChange(val value: String) : UserFormEvent()
    data class OnIdentityNumberChange(val value: String) : UserFormEvent()
    data class OnGenderChange(val value: String) : UserFormEvent()
    data class OnAddressChange(val value: String) : UserFormEvent()
    data class OnWhatsAppChange(val value: String) : UserFormEvent()
    data class OnLastEducationChange(val value: String) : UserFormEvent()
    data class OnSideJobChange(val value: String) : UserFormEvent()
    data class OnLandAreaChange(val value: String) : UserFormEvent()
    data class OnPositionChange(val value: String) : UserFormEvent()
    data class OnKphSelected(val kph: Kph) : UserFormEvent()
    data class OnKphSearchTextChange(val text: String) : UserFormEvent()
    data class OnKthSelected(val kth: Kth) : UserFormEvent()
    data class OnKthSearchTextChange(val text: String) : UserFormEvent()

    object OnTogglePasswordVisibility: UserFormEvent()
    object OnToggleConfirmPasswordVisibility: UserFormEvent()
    data object OnSubmit : UserFormEvent()
    object OnDismissMessage : UserFormEvent()
    object OnShowConfirmDialog: UserFormEvent()
    object OnDismissConfirmDialog : UserFormEvent()
    data class OnShowUserMessage(val message: String, val type: MessageType) : UserFormEvent()
}