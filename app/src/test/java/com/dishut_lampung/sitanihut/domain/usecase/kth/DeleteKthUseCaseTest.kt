package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.repository.KthRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteKthUseCaseTest {

    private lateinit var deleteKthUseCase: DeleteKthUseCase
    private val repository: KthRepository = mockk()

    @Before
    fun setUp() {
        deleteKthUseCase = DeleteKthUseCase(repository)
    }

    @Test
    fun `invoke should return Success when repository delete is successful`() = runTest {
        val id = "123"
        val expectedResult = Resource.Success(Unit)

        coEvery { repository.deleteKth(id) } returns expectedResult
        val result = deleteKthUseCase(id)
        assertTrue("Expected result to be Resource.Success", result is Resource.Success)
        coVerify(exactly = 1) { repository.deleteKth(id) }
    }

    @Test
    fun `invoke should return Error when repository delete fails`() = runTest {
        val id = "123"
        val errorMessage = "Gagal menghapus data KTH"
        val expectedResult = Resource.Error<Unit>(errorMessage)

        coEvery { repository.deleteKth(id) } returns expectedResult

        val result = deleteKthUseCase(id)
        assertTrue("Expected result to be Resource.Error", result is Resource.Error)
        assertEquals(errorMessage, result.message)
        coVerify(exactly = 1) { repository.deleteKth(id) }
    }
}