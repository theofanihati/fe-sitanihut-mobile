package com.dishut_lampung.sitanihut.domain.usecase.penyuluh

import com.dishut_lampung.sitanihut.domain.model.Penyuluh
import com.dishut_lampung.sitanihut.domain.repository.PenyuluhRepository
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

class GetPenyuluhUseCaseTest {
    private lateinit var repository: PenyuluhRepository
    private lateinit var useCase: GetPenyuluhUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetPenyuluhUseCase(repository)
    }

    @Test
    fun `invoke should return Error when user role is NOT PJ`() = runTest {
        val invalidRole = "petani"
        val result = useCase(invalidRole).first()
        assertTrue(result is Resource.Error)
        assertEquals("Anda tidak memiliki akses", result.message)

        verify(exactly = 0) { repository.getPenyuluhList() }
    }

    @Test
    fun `invoke should return Data from repository when user role is PJ`() = runTest {
        val validRole = "penanggung jawab"
        val dummyData = listOf(
            Penyuluh("1", "u1", "Budi", "Penyuluh kehutanan", "pria", "id_kph_ceritanya", "kph 1")
        )

        every { repository.getPenyuluhList() } returns flowOf(Resource.Success(dummyData))
        val result = useCase(validRole).first()
        assertTrue(result is Resource.Success)
        assertEquals(dummyData, result.data)

        verify(exactly = 1) { repository.getPenyuluhList() }
    }

}