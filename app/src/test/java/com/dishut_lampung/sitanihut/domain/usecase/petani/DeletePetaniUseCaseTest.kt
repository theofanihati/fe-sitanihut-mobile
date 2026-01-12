package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeletePetaniUseCaseTest {

    private lateinit var deletePetaniUseCase: DeletePetaniUseCase
    private val repository: PetaniRepository = mockk()

    @Before
    fun setUp() {
        deletePetaniUseCase = DeletePetaniUseCase(repository)
    }

    @Test
    fun `invoke should return Success when repository delete is successful`() = runTest {
        val id = "123"
        val expectedResult = Resource.Success(Unit)

        coEvery { repository.deletePetani(id) } returns expectedResult
        val result = deletePetaniUseCase(id)
        assertTrue("Expected result to be Resource.Success", result is Resource.Success)
        coVerify(exactly = 1) { repository.deletePetani(id) }
    }

    @Test
    fun `invoke should return Error when repository delete fails`() = runTest {
        val id = "123"
        val errorMessage = "Gagal menghapus data Petani"
        val expectedResult = Resource.Error<Unit>(errorMessage)

        coEvery { repository.deletePetani(id) } returns expectedResult

        val result = deletePetaniUseCase(id)
        assertTrue("Expected result to be Resource.Error", result is Resource.Error)
        assertEquals(errorMessage, result.message)
        coVerify(exactly = 1) { repository.deletePetani(id) }
    }
}