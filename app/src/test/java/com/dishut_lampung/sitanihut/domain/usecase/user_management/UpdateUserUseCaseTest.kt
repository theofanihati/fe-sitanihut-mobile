package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateUserUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var useCase: UpdateUserUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UpdateUserUseCase(repository)
    }

    @Test
    fun `invoke should return Success when repository update is successful`() = runTest {
        val id = "123"
        val changes = mapOf<String, Any?>("nama_petani" to "Fani Update")
        val expectedResult = Resource.Success(Unit)

        coEvery { repository.updateUser(id, changes) } returns expectedResult

        val result = useCase(id, changes)
        assertEquals(expectedResult, result)

        coVerify(exactly = 1) { repository.updateUser(id, changes) }
    }

    @Test
    fun `invoke should return Error when repository update fails`() = runTest {
        val id = "123"
        val changes = mapOf<String, Any?>("nama_petani" to "Fani Update")
        val errorMessage = "Gagal update data"
        val expectedResult = Resource.Error<Unit>(errorMessage)

        coEvery { repository.updateUser(id, changes) } returns expectedResult

        val result = useCase(id, changes)
        assertEquals(expectedResult, result)
        coVerify(exactly = 1) { repository.updateUser(id, changes) }
    }
}