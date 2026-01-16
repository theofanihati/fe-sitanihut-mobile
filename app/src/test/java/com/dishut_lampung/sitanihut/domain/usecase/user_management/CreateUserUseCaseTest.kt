package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.model.CreateUserInput
import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateUserUseCaseTest {
    private val repository: UserRepository = mockk()
    private lateinit var createUserUseCase: CreateUserUseCase

    @Before
    fun setUp() {
        createUserUseCase = CreateUserUseCase(repository)
    }

    @Test
    fun `invoke should call repository createKth`() = runTest {
        val input = mockk<CreateUserInput>(relaxed = true)
        coEvery { repository.createUser(input) } returns Resource.Success(Unit)

        val result = createUserUseCase(input)

        assertTrue(result is Resource.Success)
        coVerify(exactly = 1) { repository.createUser(input) }
    }
}
