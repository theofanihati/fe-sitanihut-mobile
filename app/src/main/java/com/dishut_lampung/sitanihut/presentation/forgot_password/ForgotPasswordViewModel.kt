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
                val emailResult = validateEmailUseCase(event.email)
                forgotPasswordState = forgotPasswordState.copy(
                    email = event.email,
                    emailError = emailResult.errorMessage,
                    generalError = null
                )
            }
            is ForgotPasswordEvent.OnSubmitClick -> {
                submitData()
            }
            else -> {}
        }
    }

    private fun submitData() {
        val emailResult = validateEmailUseCase(forgotPasswordState.email)
        val hasError = !emailResult.successful
        if (hasError) {
            forgotPasswordState = forgotPasswordState.copy(
                emailError = emailResult.errorMessage,
            )
            return
        }
        submit()
    }

    private fun submit(){
        viewModelScope.launch {
            forgotPasswordState = forgotPasswordState.copy(isLoading = true)
            val result = forgotPasswordUseCase(forgotPasswordState.email)
            when (result) {
                is AuthResult.Success -> {
                    forgotPasswordState = forgotPasswordState.copy(isLoading = false)
                    _eventFlow.emit(ForgotPasswordUiEvent.SubmitSuccess)
                }
                is AuthResult.Error -> {
                    forgotPasswordState = forgotPasswordState.copy(
                        isLoading = false,
                        generalError = result.message
                    )
                }
            }
        }
    }
}