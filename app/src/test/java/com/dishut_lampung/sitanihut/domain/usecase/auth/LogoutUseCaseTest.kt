package com.dishut_lampung.sitanihut.domain.usecase.auth

import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {
    private lateinit var logoutUseCase: LogoutUseCase
    private val mockAuthRepository = mockk<AuthRepository>(relaxed = true)

    @Before
    fun setUp() {
        logoutUseCase = LogoutUseCase(mockAuthRepository)
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun `invoke should call logout on repository`() = runTest {
        coEvery { mockAuthRepository.logout(any()) } returns Unit
        logoutUseCase()
        coVerify(exactly = 1) { mockAuthRepository.logout(any<String>()) }
    }
}