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

class GetPenyuluhDetailUseCaseTest {

    private lateinit var repository: PenyuluhRepository
    private lateinit var useCase: GetPenyuluhDetailUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetPenyuluhDetailUseCase(repository)
    }

    @Test
    fun `invoke should return Data from repository when id is provided`() = runTest {
        val dummyId = "123"
        val dummyPenyuluh = Penyuluh(
            id = dummyId,
            name = "Ahmad",
            identityNumber = "12345",
            position = "Ahli",
            gender = "pria",
            kphId = "1",
            kphName = "KPH A",
            whatsAppNumber = "08123456789"
        )

        every { repository.getPenyuluhDetail(dummyId) } returns flowOf(Resource.Success(dummyPenyuluh))

        val result = useCase(dummyId).first()
        assertTrue(result is Resource.Success)
        assertEquals(dummyPenyuluh, result.data)
        assertEquals("08123456789", result.data?.whatsAppNumber)
        verify(exactly = 1) { repository.getPenyuluhDetail(dummyId) }
    }
}