package com.dishut_lampung.sitanihut.domain.usecase

import com.dishut_lampung.sitanihut.domain.model.AuthResult
import com.dishut_lampung.sitanihut.domain.model.User
import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import com.dishut_lampung.sitanihut.domain.use_case.auth.LoginUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var loginUseCase: LoginUseCase
    private val mockAuthRepository = mockk<AuthRepository>(relaxed = true)

    @Before
    fun setUp() {
        loginUseCase = LoginUseCase(mockAuthRepository)
    }

    @Test
    fun `invoke with valid credentials should return success result`() = runTest {
        val email = "petani@example.com"
        val password = "petani123#"
        val expectedUser = User(id = "user-123", name = "Petani 1", token = "xyz-token", role = "petani", email = "petani@example.com")
        val expectedResult = AuthResult.Success(data = expectedUser)

        coEvery { mockAuthRepository.login(email, password) } returns expectedResult

        val actualResult = loginUseCase(email, password)

        assertEquals(expectedResult, actualResult)
    }

    // username benar pass salah
    @Test
    fun `invoke with correct email and wrong password should return error result`() = runTest {
        val email = "petani@example.com"
        val wrongPassword = "passwordsalah"
        val expectedErrorResult = AuthResult.Error<User>(message = "Email dan Password tidak cocok")
        coEvery { mockAuthRepository.login(email, wrongPassword) } returns expectedErrorResult

        val actualResult = loginUseCase(email, wrongPassword)

        assertEquals(expectedErrorResult, actualResult)
    }

    // username salah pass benar, username salah pass salah
    @Test
    fun `invoke with wrong email should return error result`() = runTest {
        val wrongEmail = "salah@sitanihut.com"
        val password = "petani123#"
        val expectedErrorResult = AuthResult.Error<User>(message = "Email dan Password tidak cocok")
        coEvery { mockAuthRepository.login(wrongEmail, password) } returns expectedErrorResult

        val actualResult = loginUseCase(wrongEmail, password)

        assertEquals(expectedErrorResult, actualResult)
    }

    // username kosong
    @Test
    fun `invoke with empty email should return error result`() = runTest {
        val emptyEmail = ""
        val password = "petani123#"
        val expectedErrorResult = AuthResult.Error<User>(message ="Email tidak boleh kosong")
        // repository mengembalikan error tanpa ke server
        coEvery { mockAuthRepository.login(emptyEmail, password) } returns expectedErrorResult

        val actualResult = loginUseCase("", "password123#")

        assertEquals(expectedErrorResult, actualResult)
    }

    // pw kosong
    @Test
    fun `invoke with empty password should return error result`() = runTest {
        val email = "petani@example.com"
        val emptyPassword = ""
        val expectedErrorResult = AuthResult.Error<User>(message = "Password tidak boleh kosong")
        coEvery { mockAuthRepository.login(email, emptyPassword) } returns expectedErrorResult

        val actualResult = loginUseCase("test@sitanihut.com", "")

        assertEquals(expectedErrorResult, actualResult)
    }

    // buat pw baru, login dengan pw baru
    @Test
    fun `invoke with NEW password after change should return success result`() = runTest {
        val email = "petani@example.com"
        val newPassword = "passwordBaruYangKuat"
        val expectedUser = User(id = "user-123", name = "Petani 1", token = "xyz-token", role = "petani", email = "petani@example.com")
        val expectedResult = AuthResult.Success(data = expectedUser)
        coEvery { mockAuthRepository.login(email, newPassword) } returns expectedResult

        val actualResult = loginUseCase(email, newPassword)

        assertEquals(expectedResult, actualResult)
    }

    // buat pw baru, login dengan pw lama
    @Test
    fun `invoke with OLD password after change should return error result`() = runTest {
        val email = "petani@example.com"
        val oldPassword = "password123#"
        val expectedErrorResult = AuthResult.Error<User>(message = "Email dan Password tidak cocok")
        coEvery { mockAuthRepository.login(email, oldPassword) } returns expectedErrorResult

        val actualResult = loginUseCase(email, oldPassword)

        assertEquals(expectedErrorResult, actualResult)
    }
}