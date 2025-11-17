package com.dishut_lampung.sitanihut.presentation.forgot_password

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.use_case.auth.ForgotPasswordUseCase
import com.dishut_lampung.sitanihut.domain.use_case.auth.ValidateEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
) : ViewModel() {

    var forgotPasswordState by mutableStateOf(ForgotPasswordState())
        private set

    private val _eventFlow = MutableSharedFlow<ForgotPasswordUiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.OnEmailChange -> {
                // TODO BLMMM
            }

            is ForgotPasswordEvent.OnSubmitClick -> {
                // TODO BELUUUMMM BEBBB
            }

            else -> {}
        }
    }
}