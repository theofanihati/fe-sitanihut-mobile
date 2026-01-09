package com.dishut_lampung.sitanihut.presentation.kth.form

import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.presentation.components.animations.MessageType

data class KthFormUiState (
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isFormValid: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val isEditMode: Boolean = false,

    val name: String = "",
    val nameError: String? = null,

    val kabupatenOptions: List<String> = emptyList(),
    val selectedKabupaten: String = "",
    val kabupatenError: String? = null,

    val kecamatanOptions: List<String> = emptyList(),
    val selectedKecamatan: String = "",
    val kecamatanError: String? = null,

    val desaOptions: List<String> = emptyList(),
    val selectedDesa: String = "",
    val desaError: String? = null,

    val coordinator: String = "",
    val coordinatorError: String? = null,
    val whatsappNumber: String = "",
    val whatsappError: String? = null,

    val kphOptions: List<Kph> = emptyList(),
    val selectedKphId: String = "",
    val selectedKphName: String = "",
    val kphError: String? = null,
    )

sealed class KthFormUiEvent {
    data class OnNameChange(val value: String) : KthFormUiEvent()
    data class OnDesaSelected(val value: String) : KthFormUiEvent()
    data class OnKabupatenSearchTextChange(val text: String) : KthFormUiEvent()
    data class OnKecamatanSearchTextChange(val text: String) : KthFormUiEvent()
    data class OnDesaSearchTextChange(val text: String) : KthFormUiEvent()
    data class OnKecamatanSelected(val value: String) : KthFormUiEvent()
    data class OnKabupatenSelected(val value: String) : KthFormUiEvent()
    data class OnCoordinatorChange(val value: String) : KthFormUiEvent()
    data class OnWhatsappChange(val value: String) : KthFormUiEvent()
    data class OnKphSelected(val kph: Kph) : KthFormUiEvent()
    data class OnKphSearchTextChange(val text: String) : KthFormUiEvent()

    data object OnSubmit : KthFormUiEvent()
    object OnDismissMessage : KthFormUiEvent()
    object OnShowConfirmDialog: KthFormUiEvent()
    object OnDismissConfirmDialog : KthFormUiEvent()
    data class OnShowUserMessage(val message: String, val type: MessageType) : KthFormUiEvent()
}