package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.repository.KthRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateKthUseCaseTest {

    private lateinit var repository: KthRepository
    private lateinit var useCase: UpdateKthUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UpdateKthUseCase(repository)
    }

    @Test
    fun `invoke should return Success when repository update is successful`() = runTest {
        val id = "123"
        val input = mockk<CreateKthInput>(relaxed = true)
        val expectedResult = Resource.Success(Unit)

        coEvery { repository.updateKth(id, input) } returns expectedResult

        val result = useCase(id, input)
        assertEquals(expectedResult, result)

        coVerify(exactly = 1) { repository.updateKth(id, input) }
    }

    @Test
    fun `invoke should return Error when repository update fails`() = runTest {
        val id = "123"
        val input = mockk<CreateKthInput>(relaxed = true)
        val errorMessage = "Gagal update data"
        val expectedResult = Resource.Error<Unit>(errorMessage)

        coEvery { repository.updateKth(id, input) } returns expectedResult

        val result = useCase(id, input)
        assertEquals(expectedResult, result)
        coVerify(exactly = 1) { repository.updateKth(id, input) }
    }
}