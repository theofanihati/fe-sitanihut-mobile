package com.dishut_lampung.sitanihut.presentation

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Operation
import androidx.work.WorkInfo
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.UserProfile
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.LogoutUseCase
import com.dishut_lampung.sitanihut.presentation.shared.navigation.Screen
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private lateinit var viewModel: MainViewModel
    private val homeRepository: HomeRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val logoutUseCase: LogoutUseCase = mockk(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(FirebaseMessaging::class)
        val mockFirebase = mockk<FirebaseMessaging>(relaxed = true)
        every { FirebaseMessaging.getInstance() } returns mockFirebase

        mockkStatic(android.os.Process::class)
        every { android.os.Process.myPid() } returns 1234

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        every { userPreferences.authToken } returns flowOf(null)
        every { userPreferences.lastSyncTime } returns flowOf(0L)
        every { userPreferences.userRole } returns flowOf(null)
        every { userPreferences.hasSeenOnboarding } returns flowOf(false)
        every { homeRepository.getUserProfile() } returns flowOf(
            UserProfile(
                name = "Tepani canz lo",
                role = "Petani",
                profilePictureUrl = null
            )
        )
    }

    private fun setupViewModel(){
        viewModel = MainViewModel(userPreferences, homeRepository, workManager, userRepository, logoutUseCase)
    }

    @Test
    fun `init when hasSeenOnboarding is false, startDestination should be landing screen`() {
        runTest {
            every { userPreferences.hasSeenOnboarding } returns flowOf(false)
            setupViewModel()
            val expectedDestination = Screen.LandingPage.route
            assertEquals(expectedDestination, viewModel.uiState.value.startDestination)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }
    }

    @Test
    fun `init when hasSeenOnboarding is true, startDestination should be auth`() {
        runTest {
            every { userPreferences.hasSeenOnboarding } returns flowOf(true)

            setupViewModel()
            val expectedDestination = "auth"
            assertEquals(expectedDestination, viewModel.uiState.value.startDestination)
            assertEquals(false, viewModel.uiState.value.isLoading)
        }
    }

    @Test
    fun `logout calls logoutUseCase`() = runTest {
        setupViewModel()
        coEvery { logoutUseCase.invoke() } returns Unit
        viewModel.logout()
        advanceUntilIdle()
        coVerify(exactly = 1) { logoutUseCase.invoke() }
    }

    @Test
    fun `init when HAS token and role is PETANI, startDestination should be HomePetani`() = runTest {
        every { userPreferences.authToken } returns flowOf("valid_token")
        every { userPreferences.userRole } returns flowOf("Petani") // Case insensitive check logic
        every { userPreferences.hasSeenOnboarding } returns flowOf(true)

        setupViewModel()
        advanceUntilIdle()

        assertEquals(Screen.HomePetani.route, viewModel.uiState.value.startDestination)
    }

    @Test
    fun `init when HAS token and role is PENYULUH, startDestination should be HomePenyuluh`() = runTest {
        every { userPreferences.authToken } returns flowOf("valid_token")
        every { userPreferences.userRole } returns flowOf("Penyuluh")

        setupViewModel()
        advanceUntilIdle()

        assertEquals(Screen.HomePenyuluh.route, viewModel.uiState.value.startDestination)
    }

    @Test
    fun `init when HAS token and role is PENANGGUNG JAWAB, startDestination should be HomeKkph`() = runTest {
        every { userPreferences.authToken } returns flowOf("valid_token")
        every { userPreferences.userRole } returns flowOf("Penanggung Jawab")

        setupViewModel()
        advanceUntilIdle()

        assertEquals(Screen.HomeKkph.route, viewModel.uiState.value.startDestination)
    }

    @Test
    fun `observeSessionForSync triggers Sync Worker when data expired`() = runTest {
        val mockToken = "valid_token"
        val oldSyncTime = System.currentTimeMillis() - (60 * 60 * 1000L)

        every { userPreferences.authToken } returns flowOf(mockToken)
        every { userPreferences.lastSyncTime } returns flowOf(oldSyncTime)

        val mockOperation = mockk<Operation>()
        val mockWorkInfoLiveData = MutableLiveData<WorkInfo>()

        every {
            workManager.enqueueUniqueWork("initial_data_sync", any(), any<OneTimeWorkRequest>())
        } returns mockOperation

        every {
            workManager.enqueueUniquePeriodicWork("periodic_data_sync", any(), any())
        } returns mockOperation

        every { workManager.getWorkInfoByIdLiveData(any()) } returns mockWorkInfoLiveData

        setupViewModel()
        advanceUntilIdle()

        verify {
            workManager.enqueueUniquePeriodicWork("periodic_data_sync", any(), any())
        }
        verify {
            workManager.enqueueUniqueWork("initial_data_sync", any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun `observeSessionForSync does not trigger OneTime Sync Worker when data is fresh`() = runTest {
        val mockToken = "valid_token"
        val freshSyncTime = System.currentTimeMillis() - (10 * 60 * 1000L)

        every { userPreferences.authToken } returns flowOf(mockToken)
        every { userPreferences.lastSyncTime } returns flowOf(freshSyncTime)

        val mockOperation = mockk<Operation>()
        every {
            workManager.enqueueUniquePeriodicWork("periodic_data_sync", any(), any())
        } returns mockOperation

        setupViewModel()
        advanceUntilIdle()

        verify {
            workManager.enqueueUniquePeriodicWork("periodic_data_sync", any(), any())
        }
        verify(exactly = 0) {
            workManager.enqueueUniqueWork("initial_data_sync", any(), any<OneTimeWorkRequest>())
        }
    }

    @Test
    fun `checkAndSyncFcmToken should call userRepository syncFcmToken when successful`() = runTest {
        val mockToken = "fcm_token_123"
        val mockFirebase = mockk<FirebaseMessaging>(relaxed = true)
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns mockFirebase

        val mockTask = mockk<com.google.android.gms.tasks.Task<String>>()

        every { mockFirebase.token } returns mockTask
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockToken
        every { mockTask.exception } returns null

        val slot = slot<OnCompleteListener<String>>()
        every { mockTask.addOnCompleteListener(capture(slot)) } answers {
            slot.captured.onComplete(mockTask)
            mockTask
        }
        every { userPreferences.authToken } returns flowOf("valid_token")

        setupViewModel()
        advanceUntilIdle()

        coVerify(timeout = 2000) { userRepository.syncFcmToken(mockToken) }
    }

    @Test
    fun `syncCurrentFcmToken when firebase fails, should not call repository`() = runTest {
        val mockFirebase = mockk<FirebaseMessaging>(relaxed = true)
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns mockFirebase

        val mockTask = mockk<com.google.android.gms.tasks.Task<String>>()
        every { mockFirebase.token } returns mockTask
        every { mockTask.isSuccessful } returns false

        val slot = slot<OnCompleteListener<String>>()
        every { mockTask.addOnCompleteListener(capture(slot)) } answers {
            slot.captured.onComplete(mockTask)
            mockTask
        }

        every { userPreferences.authToken } returns flowOf("valid_token")
        setupViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { userRepository.syncFcmToken(any()) }
    }

    @Test
    fun `syncCurrentFcmToken when exception repository handle`() = runTest {
        val mockToken = "fcm_token_123"
        val mockFirebase = mockk<FirebaseMessaging>(relaxed = true)
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns mockFirebase
        val mockTask = mockk<com.google.android.gms.tasks.Task<String>>()

        every { mockFirebase.token } returns mockTask
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockToken

        val slot = slot<OnCompleteListener<String>>()
        every { mockTask.addOnCompleteListener(capture(slot)) } answers {
            slot.captured.onComplete(mockTask)
            mockTask
        }

        every { userPreferences.authToken } returns flowOf("valid_token")
        coEvery { userRepository.syncFcmToken(mockToken) } throws Exception("Network Error")

        setupViewModel()
        advanceUntilIdle()
        coVerify { userRepository.syncFcmToken(mockToken) }
    }
}