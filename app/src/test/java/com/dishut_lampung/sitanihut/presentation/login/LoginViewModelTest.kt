package com.dishut_lampung.sitanihut.presentation.login

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.model.User
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.LoginUseCase
import com.dishut_lampung.sitanihut.domain.usecase.auth.ValidateLoginInputUseCase
import com.dishut_lampung.sitanihut.domain.validator.ValidationResult
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

@ExperimentalCoroutinesApi
class LoginViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private var loginUseCase: LoginUseCase = mockk()
    private var validateLoginInputUseCase: ValidateLoginInputUseCase = mockk(relaxed = true)

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(
            loginUseCase,
            validateLoginInputUseCase
        )
    }

    @Test
    fun `onEvent OnEmailChange, state should be updated with new email`() {
        val newEmail = "test@example.com"
        every { validateLoginInputUseCase(newEmail) } returns ValidationResult(successful = true)
        viewModel.onEvent(LoginEvent.OnEmailChange(newEmail))

        assertEquals(newEmail, viewModel.loginState.email)
        assertNull(viewModel.loginState.emailError)
    }

    @Test
    fun `onEvent OnPasswordChange, state should be updated with new password`() {
        val newPassword = "passwordbaru"
        viewModel.onEvent(LoginEvent.OnPasswordChange(newPassword))

        assertEquals(newPassword, viewModel.loginState.password)
        assertNull(viewModel.loginState.passwordError)
    }

    @Test
    fun `onEvent OnLoginClick with invalid email, state should show email error`() {
        val invalidEmail = "invalid-email"
        every { validateLoginInputUseCase(invalidEmail) } returns ValidationResult(false, "Format email tidak valid")

        viewModel.onEvent(LoginEvent.OnEmailChange(invalidEmail))
        viewModel.onEvent(LoginEvent.OnPasswordChange("anypassword"))

        viewModel.onEvent(LoginEvent.OnLoginClick)

        assertEquals("Format email tidak valid", viewModel.loginState.emailError)
    }

    @Test
    fun `onEvent OnLoginClick with valid data and login success, should update successMessage`() = runTest {
        val email = "valid@email.com"
        val password = "validpassword"

        every { validateLoginInputUseCase(email) } returns ValidationResult(true)

        val dummyUser = User(
            id = "1",
            name = "Test User",
            email = email,
            token = "token_123",
            role = "Petani"
        )
        coEvery { loginUseCase(email, password) } returns AuthResult.Success(dummyUser)

        viewModel.onEvent(LoginEvent.OnEmailChange(email))
        viewModel.onEvent(LoginEvent.OnPasswordChange(password))
        viewModel.onEvent(LoginEvent.OnLoginClick)

        assertEquals("Login Berhasil!", viewModel.loginState.successMessage)
        assertEquals(false, viewModel.loginState.isLoading)
    }

    @Test
    fun `onEvent OnLoginClick with valid data but login fails, state should show general error`() = runTest {
        val email = "valid@email.com"
        val password = "wrongpassword"
        every { validateLoginInputUseCase(email) } returns ValidationResult(true)

        coEvery { loginUseCase(email, password) } returns AuthResult.Error("Password salah")

        viewModel.onEvent(LoginEvent.OnEmailChange(email))
        viewModel.onEvent(LoginEvent.OnPasswordChange(password))

        viewModel.onEvent(LoginEvent.OnLoginClick)

        assertEquals("Password salah", viewModel.loginState.generalError)
        assertEquals(false, viewModel.loginState.isLoading)
    }

    @Test
    fun `onEvent OnForgotPasswordClick, should emit NavigateToForgotPassword event`() {
        runTest {
            viewModel.eventFlow.test {
                viewModel.onEvent(LoginEvent.OnForgotPasswordClick)

                val event = awaitItem()
                assertTrue(event is UiEvent.NavigateToForgotPassword)
                expectNoEvents()
            }
        }
    }

    @Test
    fun `onEvent OnTogglePasswordVisibility, state isPasswordVisible should be toggled`() {
        assertFalse(viewModel.loginState.isPasswordVisible)

        viewModel.onEvent(LoginEvent.OnTogglePasswordVisibility)
        assertTrue(viewModel.loginState.isPasswordVisible)

        viewModel.onEvent(LoginEvent.OnTogglePasswordVisibility)
        assertFalse(viewModel.loginState.isPasswordVisible)
    }

    @Test
    fun `onEvent OnDismissSuccessMessage, should emit NavigateToHome event`() {
        runTest {
            viewModel.eventFlow.test {
                viewModel.onEvent(LoginEvent.OnDismissSuccessMessage)

                val event = awaitItem()
                assertTrue(event is UiEvent.NavigateToHome)
                expectNoEvents()
            }
        }
    }
}