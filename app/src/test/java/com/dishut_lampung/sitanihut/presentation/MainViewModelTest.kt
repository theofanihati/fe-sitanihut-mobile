package com.dishut_lampung.sitanihut.presentation

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.presentation.navigation.Screen
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: MainViewModel

    @Test
    fun `init when hasSeenOnboarding is false, startDestination should be landing screen`() {
        runTest {
            userPreferences = mockk()
            every { userPreferences.hasSeenOnboarding } returns flowOf(false)
            viewModel = MainViewModel(userPreferences)

            val expectedDestination = Screen.LandingPage.route
            assertEquals(expectedDestination, viewModel.uiState.value.startDestination)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }
    }

    @Test
    fun `init when hasSeenOnboarding is true, startDestination should be auth`() {
        runTest {
            userPreferences = mockk()
            every { userPreferences.hasSeenOnboarding } returns flowOf(true)

            viewModel = MainViewModel(userPreferences)

            val expectedDestination = "auth"
            assertEquals(expectedDestination, viewModel.uiState.value.startDestination)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }
    }
}