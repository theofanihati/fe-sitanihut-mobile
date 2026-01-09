package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.domain.usecase.kth.GetKthListUseCase
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetPetaniListUseCaseTest {
    private lateinit var repository: PetaniRepository
    private lateinit var useCase: GetPetaniListUseCase

    val dummyPetani = listOf(
        Petani(
            "1",
            "Petani Suka Suka",
            "1234567890",
            "Laki-laki",
            "Jl. Suka Suka",
            "081234567890",
            "S1",
            "Petani",
            100.0,
            "1",
            "Tahura WAR",
            "1",
            "KTH Suka Suka"
        )
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetPetaniListUseCase(repository)
    }

    @Test
    fun `invoke should return error when role is not allowed`() = runTest {
        val role = "petani"
        val query = ""
        val result = useCase(role, query).first()

        assertTrue("Harusnya return Error untuk role petani", result is Resource.Error)
        verify(exactly = 0) { repository.getPetaniList(any()) }
    }

    @Test
    fun `invoke should return data when role is Penyuluh`() = runTest {
        val role = "penyuluh"
        val query = "Tahura"
        every { repository.getPetaniList(query) } returns flowOf(Resource.Success(dummyPetani))

        val result = useCase(role, query).first()

        assertTrue(result is Resource.Success)
        assertEquals(dummyPetani, result.data)
        verify { repository.getPetaniList(query) }
    }

    @Test
    fun `invoke should return data when role is Penanggung Jawab`() = runTest {
        val role = "penanggung jawab"
        val query = ""
        every { repository.getPetaniList(query) } returns flowOf(Resource.Success(emptyList()))

        val result = useCase(role, query).first()

        assertTrue(result is Resource.Success)
        verify { repository.getPetaniList(query) }
    }

    @Test
    fun `invoke should use empty query by default when query is not provided`() = runTest {
        val role = "penyuluh"
        val expectedDefaultQuery = ""
        every { repository.getPetaniList(expectedDefaultQuery) } returns flowOf(Resource.Success(dummyPetani))

        val result = useCase(role).first()
        assertTrue(result is Resource.Success)
        verify { repository.getPetaniList(expectedDefaultQuery) }
    }
}