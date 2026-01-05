package com.dishut_lampung.sitanihut.domain.usecase.kth

import com.dishut_lampung.sitanihut.domain.model.CreateKthInput
import com.dishut_lampung.sitanihut.domain.repository.KthRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateKthUseCaseTest {

    private val repository: KthRepository = mockk()
    private lateinit var createKthUseCase: CreateKthUseCase

    @Before
    fun setUp() {
        createKthUseCase = CreateKthUseCase(repository)
    }

    @Test
    fun `invoke should call repository createKth`() = runTest {
        val input = mockk<CreateKthInput>(relaxed = true)
        coEvery { repository.createKth(input) } returns Resource.Success(Unit)

        val result = createKthUseCase(input)

        assertTrue(result is Resource.Success)
        coVerify(exactly = 1) { repository.createKth(input) }
    }
}