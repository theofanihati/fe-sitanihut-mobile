package com.dishut_lampung.sitanihut.presentation.profile

import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.usecase.profile.GetMyProfileUseCase
import com.dishut_lampung.sitanihut.presentation.profile.petani.PetaniProfileViewModel
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PenyuluhProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainCoroutineRule()

    private lateinit var getUserDetailUseCase: GetMyProfileUseCase
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: PetaniProfileViewModel

    private val dummyUserId = "123"
    private val dummyUser = UserDetail(
        id = dummyUserId,
        name = "Budi",
        role = "Petani",
        profilePictureUrl = "",
        email = "test@mail.com",
        roleId = "", kphId = "", kphName = "", kthId = "", kthName = "",
        identityNumber = "", gender = "", address = "", whatsAppNumber = "",
        lastEducation = "", sideJob = "", landArea = 0.0, position = ""
    )

    @Before
    fun setUp() {
        getUserDetailUseCase = mockk()
        userPreferences = mockk()
    }

    @Test
    fun `when getProfile called and userId exists, should emit Loading then Success`() = runTest {
        every { userPreferences.userId } returns flowOf(dummyUserId)
        every { getUserDetailUseCase(dummyUserId) } returns flowOf(Resource.Success(dummyUser))

        viewModel = PetaniProfileViewModel(getUserDetailUseCase, userPreferences)

        viewModel.state.test {
            val item = awaitItem()

            if (item.isLoading) {
                assertTrue(item.isLoading)
                val successItem = awaitItem()
                assertEquals(dummyUser, successItem.user)
            } else {
                assertFalse(item.isLoading)
                assertEquals(dummyUser, item.user)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when getProfile called and error occurs, should emit Error`() = runTest {
        every { userPreferences.userId } returns flowOf(dummyUserId)
        val errorMessage = "No Internet"
        every { getUserDetailUseCase(dummyUserId) } returns flowOf(Resource.Error(errorMessage))

        viewModel = PetaniProfileViewModel(getUserDetailUseCase, userPreferences)

        viewModel.state.test {
            val item = awaitItem()

            if (item.isLoading) {
                assertTrue(item.isLoading)
                val errorItem = awaitItem()
                assertEquals(errorMessage, errorItem.generalError)
            } else {
                assertFalse(item.isLoading)
                assertEquals(errorMessage, item.generalError)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when userId is null (not logged in), should emit Error`() = runTest {
        every { userPreferences.userId } returns flowOf(null)

        viewModel = PetaniProfileViewModel(getUserDetailUseCase, userPreferences)

        viewModel.state.test {
            val item = awaitItem()

            if (item.isLoading) {
                assertTrue(item.isLoading)
                val errorItem = awaitItem()
                assertEquals("hmmm, datamu gaada nih", errorItem.generalError)
            } else {
                assertFalse(item.isLoading)
                assertEquals("hmmm, datamu gaada nih", item.generalError)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}