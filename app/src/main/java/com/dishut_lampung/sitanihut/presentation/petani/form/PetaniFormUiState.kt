package com.dishut_lampung.sitanihut.presentation.petani.form

import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.presentation.shared.components.animations.MessageType

data class PetaniFormUiState (
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isFormValid: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val isEditMode: Boolean = false,
    val isOnline: Boolean = true,

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

    val kphOptions: List<Kph> = emptyList(),
    val selectedKphId: String = "",
    val selectedKphName: String = "",
    val kphError: String? = null,

    val kthOptions: List<Kth> = emptyList(),
    val selectedKthId: String = "",
    val selectedKthName: String = "",
    val kthError: String? = null,
    )

sealed class PetaniFormEvent{
    data class OnNameChange(val value: String) : PetaniFormEvent()
    data class OnIdentityNumberChange(val value: String) : PetaniFormEvent()
    data class OnGenderChange(val value: String) : PetaniFormEvent()
    data class OnAddressChange(val value: String) : PetaniFormEvent()
    data class OnWhatsAppChange(val value: String) : PetaniFormEvent()
    data class OnLastEducationChange(val value: String) : PetaniFormEvent()
    data class OnSideJobChange(val value: String) : PetaniFormEvent()
    data class OnLandAreaChange(val value: String) : PetaniFormEvent()
    data class OnKphSelected(val kph: Kph) : PetaniFormEvent()
    data class OnKphSearchTextChange(val text: String) : PetaniFormEvent()
    data class OnKthSelected(val kth: Kth) : PetaniFormEvent()
    data class OnKthSearchTextChange(val text: String) : PetaniFormEvent()

    data object OnSubmit : PetaniFormEvent()
    object OnDismissMessage : PetaniFormEvent()
    object OnShowConfirmDialog: PetaniFormEvent()
    object OnDismissConfirmDialog : PetaniFormEvent()
    data class OnShowUserMessage(val message: String, val type: MessageType) : PetaniFormEvent()
}