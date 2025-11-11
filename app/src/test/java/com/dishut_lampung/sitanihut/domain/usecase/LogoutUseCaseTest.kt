package com.dishut_lampung.sitanihut.domain.usecase

import com.dishut_lampung.sitanihut.domain.repository.AuthRepository
import com.dishut_lampung.sitanihut.domain.use_case.LogoutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {
    private lateinit var logoutUseCase: LogoutUseCase
    private val mockAuthRepository = mockk<AuthRepository>(relaxed = true)

    @Before
    fun setUp() {
        logoutUseCase = LogoutUseCase(mockAuthRepository)
    }

    @Test
    fun `invoke should call logout on repository`() = runTest {
        coEvery { mockAuthRepository.logout() } returns Unit
        logoutUseCase()

        // dipanggil sekali
        coVerify(exactly = 1) { mockAuthRepository.logout() }
    }
}