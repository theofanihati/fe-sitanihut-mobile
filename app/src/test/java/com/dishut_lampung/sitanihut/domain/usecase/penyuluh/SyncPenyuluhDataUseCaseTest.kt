package com.dishut_lampung.sitanihut.domain.usecase.penyuluh

import com.dishut_lampung.sitanihut.domain.repository.PenyuluhRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncPenyuluhDataUseCaseTest {
    private val repository: PenyuluhRepository = mockk()
    private lateinit var useCase: SyncPenyuluhDataUseCase

    @Before
    fun setUp() {
        useCase = SyncPenyuluhDataUseCase(repository)
    }

    @Test
    fun `invoke should return Success when repository sync is successful`() = runTest {
        val expectedResult = Resource.Success(Unit)
        coEvery { repository.syncPenyuluhData() } returns expectedResult

        val result = useCase()
        assertEquals(expectedResult, result)
        coVerify(exactly = 1) { repository.syncPenyuluhData() }
    }

    @Test
    fun `invoke should return Error when repository sync fails`() = runTest {
        val errorMessage = "No Internet Connection"
        val expectedResult = Resource.Error<Unit>(errorMessage)
        coEvery { repository.syncPenyuluhData() } returns expectedResult

        val result = useCase()

        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
        coVerify(exactly = 1) { repository.syncPenyuluhData() }
    }
}