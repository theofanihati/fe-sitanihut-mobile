package com.dishut_lampung.sitanihut.presentation.information

import com.dishut_lampung.sitanihut.presentation.login.LoginEvent

data class InformationState(
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val successMessage: String? = null
)

sealed class InformationEvent {
    object onDownloadClick: InformationEvent()
    object OnDismissError : InformationEvent()
    object OnDismissSuccessMessage : InformationEvent()
}