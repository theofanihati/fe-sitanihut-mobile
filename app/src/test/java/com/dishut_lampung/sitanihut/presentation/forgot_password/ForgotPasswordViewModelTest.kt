package com.dishut_lampung.sitanihut.presentation.forgot_password

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.use_case.auth.ForgotPasswordUseCase
import com.dishut_lampung.sitanihut.domain.use_case.auth.ValidateEmailUseCase
import com.dishut_lampung.sitanihut.domain.validator.ValidationResult
import com.dishut_lampung.sitanihut.presentation.login.LoginEvent
import com.dishut_lampung.sitanihut.presentation.login.UiEvent
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ForgotPasswordViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var forgotPasswordUseCase: ForgotPasswordUseCase
    private lateinit var validateEmailUseCase: ValidateEmailUseCase

    private lateinit var viewModel: ForgotPasswordViewModel

    @Before
    fun setUp() {
        forgotPasswordUseCase = mockk()
        validateEmailUseCase = mockk(relaxed = true)

        viewModel = ForgotPasswordViewModel(
            forgotPasswordUseCase,
            validateEmailUseCase,
        )
    }

    @Test
    fun `onEvent OnEmailChange, state should be updated with new email`() {
        val newEmail = "test@example.com"
        every { validateEmailUseCase(newEmail) } returns ValidationResult(successful = true)
        viewModel.onEvent(ForgotPasswordEvent.OnEmailChange(newEmail))

        assertEquals(newEmail, viewModel.forgotPasswordState.email)
        assertNull(viewModel.forgotPasswordState.emailError)
    }

    @Test
    fun `onEvent OnSubmitClick with invalid email, state should show email error`() {
        val invalidEmail = "invalid-email"
        every { validateEmailUseCase(invalidEmail) } returns ValidationResult(false, "Format email tidak valid")

        viewModel.onEvent(ForgotPasswordEvent.OnEmailChange(invalidEmail))

        viewModel.onEvent(ForgotPasswordEvent.OnSubmitClick)

        assertEquals("Format email tidak valid", viewModel.forgotPasswordState.emailError)
    }

    @Test
    fun `onEvent OnSubmitClick with valid data and data submission success, should update successMessage`() = runTest {
        val email = "valid@email.com"

        every { validateEmailUseCase(email) } returns ValidationResult(true)
        coEvery { forgotPasswordUseCase(email) } returns AuthResult.Success(Unit)

        viewModel.onEvent(ForgotPasswordEvent.OnEmailChange(email))
        viewModel.onEvent(ForgotPasswordEvent.OnSubmitClick)

        assertEquals("email telah dikirim", viewModel.forgotPasswordState.successMessage)
        assertEquals(false, viewModel.forgotPasswordState.isLoading)
    }

    @Test
    fun `onEvent OnDismissSuccessMessage, should emit SubmitSuccess event`() {
        runTest {
            viewModel.eventFlow.test {
                viewModel.onEvent(ForgotPasswordEvent.OnDismissSuccessMessage)

                val event = awaitItem()
                assertTrue(event is ForgotPasswordUiEvent.SubmitSuccess)
                expectNoEvents()
            }
        }
    }

}