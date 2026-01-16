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

class DeleteUserUseCaseTest {

    private lateinit var deleteUseCase: DeleteUserUseCase
    private val repository: UserRepository = mockk()

    @Before
    fun setUp() {
        deleteUseCase = DeleteUserUseCase(repository)
    }

    @Test
    fun `invoke should return Success when repository delete is successful`() = runTest {
        val id = "123"
        val expectedResult = Resource.Success(Unit)

        coEvery { repository.deleteUser(id) } returns expectedResult
        val result = deleteUseCase(id)
        assertTrue("Expected result to be Resource.Success", result is Resource.Success)
        coVerify(exactly = 1) { repository.deleteUser(id) }
    }

    @Test
    fun `invoke should return Error when repository delete fails`() = runTest {
        val id = "123"
        val errorMessage = "Gagal menghapus data Petani"
        val expectedResult = Resource.Error<Unit>(errorMessage)

        coEvery { repository.deleteUser(id) } returns expectedResult

        val result = deleteUseCase(id)
        assertTrue("Expected result to be Resource.Error", result is Resource.Error)
        assertEquals(errorMessage, result.message)
        coVerify(exactly = 1) { repository.deleteUser(id) }
    }
}