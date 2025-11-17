package com.dishut_lampung.sitanihut.presentation.login

data class LoginState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val generalError: String? = null,
    val successMessage: String? = null
)

sealed class LoginEvent {
    data class OnEmailChange(val email: String) : LoginEvent()
    data class OnPasswordChange(val password: String) : LoginEvent()
    object OnLoginClick : LoginEvent()
    object OnForgotPasswordClick : LoginEvent()
    object OnTogglePasswordVisibility : LoginEvent()
    object OnDismissError : LoginEvent()
    object OnDismissSuccessMessage : LoginEvent()
}