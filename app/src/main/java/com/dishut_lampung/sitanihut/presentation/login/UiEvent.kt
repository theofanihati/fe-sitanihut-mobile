package com.dishut_lampung.sitanihut.presentation.login

sealed interface UiEvent {
    data object LoginSuccess : UiEvent
    data object NavigateToForgotPassword : UiEvent
}