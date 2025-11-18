package com.dishut_lampung.sitanihut.presentation.login

sealed interface UiEvent {
    data class NavigateToHome(val route: String) : UiEvent
    data object NavigateToForgotPassword : UiEvent
}