package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.repository.UserRepository
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

class GetUserListUseCaseTest {
    private lateinit var repository: UserRepository
    private lateinit var useCase: GetUserListUseCase

    private val dummyUser = listOf(
        UserDetail(
            id= "1",
            email = "petani@gmail.com",
            roleId = "1",
            role = "petani",
            name = "Petani ganteng",
            identityNumber = "1234567890",
            gender = "Laki-laki",
            address = "Jl. Suka Suka",
            whatsAppNumber = "081234567890",
            lastEducation = "S1",
            landArea = 100.0,
            kphName = "Tahura WAR",
            kthName = "KTH nya tahura",
        )
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetUserListUseCase(repository)
    }

    @Test
    fun `invoke should return error when role is not allowed`() = runTest {
        val role = "petani"
        val query = ""
        val result = useCase(role, query).first()

        assertTrue("Harusnya return Error untuk role petani", result is Resource.Error)
        verify(exactly = 0) { repository.getUserList(any()) }
    }

    @Test
    fun `invoke should return data when role is Penyuluh`() = runTest {
        val role = "penyuluh"
        val query = "Tahura"
        every { repository.getUserList(query) } returns flowOf(Resource.Success(dummyUser))

        val result = useCase(role, query).first()

        assertTrue(result is Resource.Success)
        assertEquals(dummyUser, result.data)
        verify { repository.getUserList(query) }
    }

    @Test
    fun `invoke should return data when role is Penanggung Jawab`() = runTest {
        val role = "penanggung jawab"
        val query = ""
        every { repository.getUserList(query) } returns flowOf(Resource.Success(emptyList()))

        val result = useCase(role, query).first()

        assertTrue(result is Resource.Success)
        verify { repository.getUserList(query) }
    }

    @Test
    fun `invoke should use empty query by default when query is not provided`() = runTest {
        val role = "penyuluh"
        val expectedDefaultQuery = ""
        every { repository.getUserList(expectedDefaultQuery) } returns flowOf(Resource.Success(dummyUser))

        val result = useCase(role).first()
        assertTrue(result is Resource.Success)
        verify { repository.getUserList(expectedDefaultQuery) }
    }
}