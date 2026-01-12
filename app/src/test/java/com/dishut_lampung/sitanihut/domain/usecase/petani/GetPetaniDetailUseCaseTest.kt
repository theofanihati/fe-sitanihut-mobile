package com.dishut_lampung.sitanihut.domain.usecase.petani

import com.dishut_lampung.sitanihut.domain.model.Petani
import com.dishut_lampung.sitanihut.domain.repository.PetaniRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetPetaniDetailUseCaseTest {

    private val repository: PetaniRepository = mockk()
    private val useCase = GetPetaniDetailUseCase(repository)

    @Test
    fun `should get specific Petani from repository`() = runTest {
        val id = "123"
        val expectedPetani = Petani(
            id = id,
            name = "Petani A",
            identityNumber = "123456789",
            gender = "Laki-laki",
            address = "Alamat A",
            whatsAppNumber = "08123456789",
            lastEducation = "S1",
            sideJob = "Petani",
            landArea = 100.0,
            kphId = "123",
            kphName = "KPH X",
            kthId = "456",
            kthName = "KTH K"
        )

        every { repository.getPetaniDetail(id) } returns flowOf(Resource.Success(expectedPetani))

        val result = useCase(id).first()
        assertEquals(expectedPetani, result.data)
        verify { repository.getPetaniDetail(id) }
    }
}