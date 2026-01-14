package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdatePetaniUseCaseTest {

    private lateinit var repository: PetaniRepository
    private lateinit var useCase: UpdatePetaniUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UpdatePetaniUseCase(repository)
    }

    @Test
    fun `invoke should return Success when repository update is successful`() = runTest {
        val id = "123"
        val changes = mapOf<String, Any?>("nama_petani" to "Fani Update")
        val expectedResult = Resource.Success(Unit)

        coEvery { repository.updatePetani(id, changes) } returns expectedResult

        val result = useCase(id, changes)
        assertEquals(expectedResult, result)

        coVerify(exactly = 1) { repository.updatePetani(id, changes) }
    }

    @Test
    fun `invoke should return Error when repository update fails`() = runTest {
        val id = "123"
        val changes = mapOf<String, Any?>("nama_petani" to "Fani Update")
        val errorMessage = "Gagal update data"
        val expectedResult = Resource.Error<Unit>(errorMessage)

        coEvery { repository.updatePetani(id, changes) } returns expectedResult

        val result = useCase(id, changes)
        assertEquals(expectedResult, result)
        coVerify(exactly = 1) { repository.updatePetani(id, changes) }
    }
}