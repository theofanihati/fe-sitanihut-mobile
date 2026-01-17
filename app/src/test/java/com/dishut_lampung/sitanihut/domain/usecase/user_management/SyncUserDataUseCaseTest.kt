package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncUserDataUseCaseTest {
    private val repository: UserRepository = mockk()
    private lateinit var useCase: SyncUserDataUseCase

    @Before
    fun setUp() {
        useCase = SyncUserDataUseCase(repository)
    }

    @Test
    fun `invoke should return Success when repository sync is successful`() = runTest {
        val expectedResult = Resource.Success(Unit)
        coEvery { repository.syncUserData() } returns expectedResult

        val result = useCase()
        assertEquals(expectedResult, result)
        coVerify(exactly = 1) { repository.syncUserData() }
    }

    @Test
    fun `invoke should return Error when repository sync fails`() = runTest {
        val errorMessage = "No Internet Connection"
        val expectedResult = Resource.Error<Unit>(errorMessage)
        coEvery { repository.syncUserData() } returns expectedResult

        val result = useCase()

        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
        coVerify(exactly = 1) { repository.syncUserData() }
    }
}