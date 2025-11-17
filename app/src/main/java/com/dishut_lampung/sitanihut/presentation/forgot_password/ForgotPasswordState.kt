package com.dishut_lampung.sitanihut.presentation.forgot_password

data class ForgotPasswordState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val successMessage: String? = null
)

sealed class ForgotPasswordEvent {
    data class OnEmailChange(val email: String) : ForgotPasswordEvent()
    object OnSubmitClick : ForgotPasswordEvent()
    object OnDismissError : ForgotPasswordEvent()
    object OnDismissSuccessMessage : ForgotPasswordEvent()
}