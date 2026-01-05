package com.dishut_lampung.sitanihut.domain.usecase.kph

import com.dishut_lampung.sitanihut.domain.model.Kph
import com.dishut_lampung.sitanihut.domain.repository.KphRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetKphListUseCaseTest {
    private val repository: KphRepository = mockk(relaxed = true)
    private val useCase = GetKphListUseCase(repository)

    @Test
    fun `should return list of KPH from repository`() = runTest {
        val dummyList = listOf(Kph("1", "KPH A"), Kph("2", "KPH B"))
        every { repository.getKphList() } returns flowOf(dummyList)

        val result = useCase().first()

        assertEquals(dummyList, result)
        verify { repository.getKphList() }
    }
}