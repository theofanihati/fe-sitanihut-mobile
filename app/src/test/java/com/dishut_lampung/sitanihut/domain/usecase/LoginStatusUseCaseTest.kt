package com.dishut_lampung.sitanihut.domain.usecase

import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import com.dishut_lampung.sitanihut.domain.use_case.LoginStatusUseCase
import io.mockk.mockk
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LoginStatusUseCaseTest {
    private lateinit var loginStatusUseCase: LoginStatusUseCase
    private val mockAuthRepository = mockk<AuthRepository>(relaxed = true)

    @Before
    fun setUp() {
        loginStatusUseCase = LoginStatusUseCase(mockAuthRepository)
    }

    @Test
    fun `invoke when user is logged in should return true`() = runTest {
        coEvery { mockAuthRepository.isLoggedIn() } returns true

        val isLoggedIn = loginStatusUseCase()

        assertEquals(true, isLoggedIn)
    }

    @Test
    fun `invoke when user is not logged in should return false`() = runTest {
        coEvery { mockAuthRepository.isLoggedIn() } returns false // Merah

        val isLoggedIn = loginStatusUseCase()

        assertEquals(false, isLoggedIn)
    }
}