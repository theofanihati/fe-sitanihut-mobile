package com.dishut_lampung.sitanihut.presentation.forgot_password

sealed interface ForgotPasswordUiEvent {
    data object SubmitSuccess : ForgotPasswordUiEvent
    data object NavigateBack : ForgotPasswordUiEvent
}