package com.dishut_lampung.sitanihut.presentation.login

sealed interface UiEvent {
    data object NavigateToHome : UiEvent
    data object NavigateToForgotPassword : UiEvent
}