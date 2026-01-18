package com.dishut_lampung.sitanihut.presentation.user_management

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.UserDetail
import com.dishut_lampung.sitanihut.domain.usecase.user_management.GetUserDetailUseCase
import com.dishut_lampung.sitanihut.presentation.shared.navigation.Screen
import com.dishut_lampung.sitanihut.presentation.user_management.detail.UserDetailViewModel
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.dishut_lampung.sitanihut.util.Resource
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserDetailViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val getUserDetailUseCase: GetUserDetailUseCase = mockk(relaxed = true)
    private var userPreferences: UserPreferences = mockk()
    private var savedStateHandle: SavedStateHandle = mockk()
    private lateinit var viewModel: UserDetailViewModel

    @Before
    fun setUp() {
        every { userPreferences.userRole } returns flowOf("penyuluh")
    }

    @Test
    fun `init should load user detail successfully`() = runTest {
        val id = "123"
        val dummyData = UserDetail(
            id = "123",
            email = "petani@gmail.com",
            roleId = "1",
            role = "petani",
            kphId = "1",
            kphName = "KPH A",
            kthId = "1",
            kthName = "KTH A",
            name = "Yunita Rosa",
            identityNumber = "1234567890123456",
            gender = "wanita",
            address = "Jl. Tupai no 2",
            whatsAppNumber = "081234567890",
            lastEducation = "SMA",
            sideJob = "florist",
            landArea = 5.0,
        )

        every { savedStateHandle.get<String>("id") } returns id
        every { getUserDetailUseCase(id) } returns flowOf(
            Resource.Loading(),
            Resource.Success(dummyData)
        )

        viewModel = UserDetailViewModel(getUserDetailUseCase, userPreferences, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertEquals(dummyData, state.user)
            assertNull(state.error)
            assertEquals("penyuluh", state.userRole)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init should set error state when usecase throws exception`() = runTest {
        val id = "123"
        val errorMessage = "Database Error"

        every { savedStateHandle.get<String>("id") } returns id
        every { getUserDetailUseCase(id) } returns flow {
            throw RuntimeException(errorMessage)
        }

        viewModel = UserDetailViewModel(getUserDetailUseCase, userPreferences, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertEquals(errorMessage, state.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init should handle data not found (null)`() = runTest {
        val id = "999"
        every { savedStateHandle.get<String>("id") } returns id
        every { getUserDetailUseCase(id) } returns flowOf(
            Resource.Loading(),
            Resource.Error("Data tidak ditemukan")
        )

        viewModel = UserDetailViewModel(getUserDetailUseCase, userPreferences, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertNull(state.user)
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test
    fun `onRetry should trigger load detail again`() = runTest {
        val id = "123"
        val dummyData = UserDetail(
            id = "123",
            email = "petani@gmail.com",
            roleId = "1",
            role = "petani",
            kphId = "1",
            kphName = "KPH A",
            kthId = "1",
            kthName = "KTH A",
            name = "Yunita Rosa",
            identityNumber = "1234567890123456",
            gender = "wanita",
            address = "Jl. Tupai no 2",
            whatsAppNumber = "081234567890",
            lastEducation = "SMA",
            sideJob = "florist",
            landArea = 5.0,
        )

        every { savedStateHandle.get<String>("id") } returns id
        var callCount = 0
        every { getUserDetailUseCase(id) } answers {
            callCount++
            if (callCount == 1) {
                flowOf(
                    Resource.Loading(),
                    Resource.Error("Error Pertama")
                )
            } else {
                flowOf(
                    Resource.Loading(),
                    Resource.Success(dummyData)
                )
            }
        }

        viewModel = UserDetailViewModel(getUserDetailUseCase, userPreferences, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) state = awaitItem()
            assertEquals("Error Pertama", state.error)
            assertNull(state.user)
        }

        viewModel.onRetry()
        viewModel.uiState.test {
            var state = awaitItem()
            if (!state.isLoading) {
                if (state.error != null) state = awaitItem()
            }

            while (state.isLoading) state = awaitItem()
            assertEquals(dummyData, state.user)
            assertNull(state.error)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 2) { getUserDetailUseCase(id) }
    }

    @Test
    fun `init should set default error message when exception message is null`() = runTest {
        val id = "123"

        every { savedStateHandle.get<String>("id") } returns id
        every { getUserDetailUseCase(id) } returns flow { throw RuntimeException() }

        viewModel = UserDetailViewModel(getUserDetailUseCase, userPreferences, savedStateHandle)
        viewModel.uiState.test {
            var state = awaitItem()
            while(state.isLoading) state = awaitItem()

            assertFalse(state.isLoading)
            assertEquals("Terjadi kesalahan", state.error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}