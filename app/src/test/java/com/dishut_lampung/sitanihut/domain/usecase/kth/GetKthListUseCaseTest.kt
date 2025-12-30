package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.repository.KthRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetKthListUseCaseTest {

    private lateinit var repository: KthRepository
    private lateinit var useCase: GetKthListUseCase

    val dummyKth = listOf(
        Kth("1", "KTH Suka Suka", "Desa A", "Kemiling", "Bandar Lampung", "Ini Koor ABC","081355627894","Tahura WAR")
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetKthListUseCase(repository)
    }

    @Test
    fun `invoke should return error when role is not allowed`() = runTest {
        val role = "petani"
        val query = ""
        val result = useCase(role, query).first()

        assertTrue("Harusnya return Error untuk role petani", result is Resource.Error)
        verify(exactly = 0) { repository.getKthList(any()) }
    }

    @Test
    fun `invoke should return data when role is Penyuluh`() = runTest {
        val role = "penyuluh"
        val query = "Tahura"
        every { repository.getKthList(query) } returns flowOf(Resource.Success(dummyKth))


        val result = useCase(role, query).first()

        assertTrue(result is Resource.Success)
        assertEquals(dummyKth, result.data)
        verify { repository.getKthList(query) }
    }

    @Test
    fun `invoke should return data when role is Penanggung Jawab`() = runTest {
        val role = "penanggung jawab"
        val query = ""
        every { repository.getKthList(query) } returns flowOf(Resource.Success(emptyList()))

        val result = useCase(role, query).first()

        assertTrue(result is Resource.Success)
        verify { repository.getKthList(query) }
    }

    @Test
    fun `invoke should use empty query by default when query is not provided`() = runTest {
        val role = "penyuluh"
        val expectedDefaultQuery = ""
        every { repository.getKthList(expectedDefaultQuery) } returns flowOf(Resource.Success(dummyKth))

        val result = useCase(role).first()
        assertTrue(result is Resource.Success)
        verify { repository.getKthList(expectedDefaultQuery) }
    }
}