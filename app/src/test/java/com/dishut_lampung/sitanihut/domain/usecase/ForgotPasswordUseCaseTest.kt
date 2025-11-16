package com.dishut_lampung.sitanihut.domain.usecase

import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import com.dishut_lampung.sitanihut.domain.use_case.auth.ForgotPasswordUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ForgotPasswordUseCaseTest {
    private lateinit var forgotPasswordUseCase: ForgotPasswordUseCase
    private val mockAuthRepository = mockk<AuthRepository>(relaxed = true)

    @Before
    fun setUp() {
        forgotPasswordUseCase = ForgotPasswordUseCase(mockAuthRepository)
    }

    @Test
    fun `invoke with valid registered email should return success`() = runTest {
        val email = "user.terdaftar@sitanihut.com"
        val expectedResult = AuthResult.Success(data = Unit)
        coEvery { mockAuthRepository.requestPasswordReset(email) } returns expectedResult

        val actualResult = forgotPasswordUseCase(email)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `invoke with unregistered email should return error`() = runTest {
        val email = "tidak.terdaftar@sitanihut.com"
        val expectedError = AuthResult.Error<Unit>("Email tidak terdaftar")
        coEvery { mockAuthRepository.requestPasswordReset(email) } returns expectedError

        val actualResult = forgotPasswordUseCase(email)

        assertEquals(expectedError, actualResult)
    }

    @Test
    fun `invoke with empty email should return validation error`() = runTest {
        val expectedError = AuthResult.Error<Unit>("Email tidak boleh kosong")

        val actualResult = forgotPasswordUseCase("")

        assertEquals(expectedError, actualResult)
        coVerify(exactly = 0) { mockAuthRepository.requestPasswordReset(any()) }
    }

    @Test
    fun `invoke with invalid email format should return validation error`() = runTest {
        val invalidEmail = "ini.bukan.email"
        val expectedError = AuthResult.Error<Unit>("Format email tidak valid")

        val actualResult = forgotPasswordUseCase(invalidEmail)

        assertEquals(expectedError, actualResult)
        coVerify(exactly = 0) { mockAuthRepository.requestPasswordReset(any()) }
    }
}