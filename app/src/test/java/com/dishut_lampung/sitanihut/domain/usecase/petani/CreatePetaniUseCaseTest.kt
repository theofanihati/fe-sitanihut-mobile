package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.model.CreatePetaniInput
import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreatePetaniUseCaseTest {
    private val repository: PetaniRepository = mockk()
    private lateinit var createKthUseCase: CreatePetaniUseCase

    @Before
    fun setUp() {
        createKthUseCase = CreatePetaniUseCase(repository)
    }

    @Test
    fun `invoke should call repository createKth`() = runTest {
        val input = mockk<CreatePetaniInput>(relaxed = true)
        coEvery { repository.createPetani(input) } returns Resource.Success(Unit)

        val result = createKthUseCase(input)

        assertTrue(result is Resource.Success)
        coVerify(exactly = 1) { repository.createPetani(input) }
    }
}
