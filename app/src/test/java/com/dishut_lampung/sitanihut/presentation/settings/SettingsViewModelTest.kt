package com.dishut_lampung.sitanihut.presentation.settings

import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import com.dishut_lampung.sitanihut.domain.usecase.settings.GetSyncNetworkTypeUseCase
import com.dishut_lampung.sitanihut.domain.usecase.settings.SaveSyncNetworkTypeUseCase
import com.dishut_lampung.sitanihut.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var getSyncNetworkTypeUseCase: GetSyncNetworkTypeUseCase
    private lateinit var saveSyncNetworkTypeUseCase: SaveSyncNetworkTypeUseCase
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        getSyncNetworkTypeUseCase = mockk()
        saveSyncNetworkTypeUseCase = mockk()
        every { getSyncNetworkTypeUseCase() } returns flowOf(SyncNetworkType.ANY_NETWORK)

        viewModel = SettingsViewModel(
            getSyncNetworkTypeUseCase,
            saveSyncNetworkTypeUseCase
        )
    }

    @Test
    fun `networkPreference state flow is initialized correctly`() = runTest {
        assertEquals(SyncNetworkType.ANY_NETWORK, viewModel.networkPreference.value)
    }

    @Test
    fun `onNetworkPreferenceChanged should call save use case`() = runTest {
        val newType = SyncNetworkType.WIFI_ONLY
        coEvery { saveSyncNetworkTypeUseCase(newType) } returns Unit

        viewModel.onNetworkPreferenceChanged(newType)
        advanceUntilIdle()
        coVerify(exactly = 1) { saveSyncNetworkTypeUseCase(newType) }
    }
}