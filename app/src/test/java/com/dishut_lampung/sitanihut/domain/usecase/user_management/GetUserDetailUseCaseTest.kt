package com.dishut_lampung.sitanihut.domain.usecase.user_management

import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.domain.usecase.petani.GetPetaniDetailUseCase
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetUserDetailUseCaseTest {

    private val repository: UserRepository = mockk()
    private val useCase = GetUserDetailUseCase(repository)

    @Test
    fun `should get specific User from repository`() = runTest {
        val id = "123"
        val expectedUser = UserDetail(
            id = id,
            email = "petani@gmail.com",
            roleId = "1",
            role = "petani",
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

        every { repository.getUserDetail(id) } returns flowOf(Resource.Success(expectedUser))

        val result = useCase(id).first()
        assertEquals(expectedUser, result.data)
        verify { repository.getUserDetail(id) }
    }
}