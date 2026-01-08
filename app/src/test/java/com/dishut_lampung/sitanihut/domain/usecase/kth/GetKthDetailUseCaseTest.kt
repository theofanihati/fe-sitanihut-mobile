package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.model.Kth
import com.dishut_lampung.sitanihut.domain.repository.KthRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetKthDetailUseCaseTest {

    private val repository: KthRepository = mockk()
    private val useCase = GetKthDetailUseCase(repository)

    @Test
    fun `should get specific KTH from repository`() = runTest {
        val id = "123"
        val expectedKth = Kth(
            id = id,
            name = "KTH K",
            desa = "Desa A",
            kecamatan = "Kec A",
            kabupaten = "Kab A",
            coordinator = "Ketua A",
            whatsappNumber = "081",
            kphName = "KPH X"
        )

        every { repository.getKthDetail(id) } returns flowOf(Resource.Success(expectedKth))

        val result = useCase(id).first()
        assertEquals(expectedKth, result.data)
        verify { repository.getKthDetail(id) }
    }
}