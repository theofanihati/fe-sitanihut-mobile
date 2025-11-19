package com.dishut_lampung.sitanihut.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.usecase.auth.LoginUseCase
import com.dishut_lampung.sitanihut.domain.usecase.auth.ValidateEmailUseCase
import com.dishut_lampung.sitanihut.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
) : ViewModel() {

    private var pendingDestinationRoute: String = "home_screen_petani"

    var loginState by mutableStateOf(LoginState())
        private set

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailChange -> {
                val emailResult = validateEmailUseCase(event.email)
                loginState = loginState.copy(
                    email = event.email,
                    emailError = emailResult.errorMessage,
                    generalError = null
                )
            }
            is LoginEvent.OnPasswordChange -> {
                val isPasswordEmpty = event.password.isBlank()
                val passwordErrorMsg = if (isPasswordEmpty) {
                    "Password tidak boleh kosong"
                } else {
                    null
                }

                loginState = loginState.copy(
                    password = event.password,
                    passwordError = passwordErrorMsg,
                    generalError = null
                )
            }
            is LoginEvent.OnLoginClick -> {
                submitData()
            }
            is LoginEvent.OnForgotPasswordClick -> {
                viewModelScope.launch {
                    _eventFlow.emit(UiEvent.NavigateToForgotPassword)
                }
            }
            is LoginEvent.OnTogglePasswordVisibility -> {
                loginState = loginState.copy(
                    isPasswordVisible = !loginState.isPasswordVisible
                )
            }
            is LoginEvent.OnDismissError -> {
                loginState = loginState.copy(generalError = null)
            }
            is LoginEvent.OnDismissSuccessMessage -> {
                loginState = loginState.copy(successMessage = null)
                viewModelScope.launch {
                    _eventFlow.emit(UiEvent.NavigateToHome(pendingDestinationRoute))
                }
            }
            else -> {}
        }
    }

    private fun submitData() {
        val emailResult = validateEmailUseCase(loginState.email)
        val isPasswordEmpty = loginState.password.isBlank()

        val hasError = !emailResult.successful || isPasswordEmpty

        if (hasError) {
            loginState = loginState.copy(
                emailError = emailResult.errorMessage,
                passwordError = if (isPasswordEmpty) "Password tidak boleh kosong" else null
            )
            return
        }
        login()
    }

    private fun login() {
        viewModelScope.launch {
            loginState = loginState.copy(isLoading = true)
            val result = loginUseCase(loginState.email.trim(), loginState.password.trim())
            when (result) {
                is AuthResult.Success -> {
                    val role = result.data.role.lowercase()
                    pendingDestinationRoute = when (role) {
                        "petani" -> Screen.HomePetani.route
                        "penyuluh" -> Screen.HomePenyuluh.route
                        "kkph" -> Screen.HomeKkph.route
                        else -> Screen.HomePetani.route
                    }

                    loginState = loginState.copy(
                        isLoading = false,
                        successMessage = "Login Berhasil!"
                    )
                }
                is AuthResult.Error -> {
                    loginState = loginState.copy(
                        isLoading = false,
                        generalError = result.message
                    )
                }
            }
        }
    }
}