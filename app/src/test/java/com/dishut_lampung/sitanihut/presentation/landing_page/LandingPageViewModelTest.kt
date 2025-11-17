package com.dishut_lampung.sitanihut.presentation.landing_page

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LandingPageViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: LandingPageViewModel

    @Before
    fun setUp() {
        userPreferences = mockk(relaxed = true)
        viewModel = LandingPageViewModel(userPreferences)
    }

    @Test
    fun `on setOnboardingCompleted, should call repository to save flag`() {
        runTest {
            viewModel.setOnboardingCompleted()

            coVerify(exactly = 1) { userPreferences.setOnboardingCompleted() }
        }
    }
}