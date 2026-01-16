package com.dishut_lampung.sitanihut.domain.usecase.profile


import app.cash.turbine.test
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.repository.ProfileRepository
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetMyProfileUseCaseTest {

    private lateinit var mockProfileRepository: ProfileRepository
    private lateinit var getUserDetailUseCase: GetMyProfileUseCase

    @Before
    fun setUp() {
        mockProfileRepository = mockk()
        getUserDetailUseCase = GetMyProfileUseCase(mockProfileRepository)
    }

    @Test
    fun `invoke should return user detail from repository successfully`() = runTest {
        val dummyUserId = "user-123"
        val dummyUserDetail = UserDetail(
            id = dummyUserId,
            email= "Petani@gmail.com",
            roleId = "id-petani",
            role = "petani",
            kphName = "KPH 123",
            kphId = "kph-123",
            kthName = "KTH 456",
            kthId = "kth-456",
            name = "Petani Test",
            identityNumber = "1234567890",
            gender = "pria",
            address = "dimana ya",
            whatsAppNumber = "08123456789",
            lastEducation = "SD",
            sideJob = "influencer",
            landArea = 8.06,)
        val expectedFlow = flowOf(Resource.Success(dummyUserDetail))
        coEvery { mockProfileRepository.getUserDetail(dummyUserId) } returns expectedFlow

        val resultFlow = getUserDetailUseCase(dummyUserId)

        resultFlow.test {
            val result = awaitItem()
            val expectedResult = Resource.Success(successData = dummyUserDetail)
            val successResult = result as Resource.Success<UserDetail>
            assertEquals(expectedResult, successResult)

            awaitComplete()
        }
    }

    @Test
    fun `invoke should return error when repository returns error`() = runTest {
        val dummyUserId = "user-123"
        val errorMessage = "Network Error"
        val expectedFlow = flowOf(Resource.Error<UserDetail>(errorMessage))
        coEvery { mockProfileRepository.getUserDetail(dummyUserId) } returns expectedFlow

        val resultFlow = getUserDetailUseCase(dummyUserId)

        resultFlow.test {
            val errorResult = awaitItem()
            assertEquals(errorMessage, (errorResult as Resource.Error).message)
            awaitComplete()
        }
    }
}