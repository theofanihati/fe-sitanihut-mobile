package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsRepositoryImplTest {

    private lateinit var userPreferences: UserPreferences
    private lateinit var settingsRepositoryImpl: SettingsRepositoryImpl

    @Before
    fun setUp() {
        userPreferences = mockk(relaxed = true)
        settingsRepositoryImpl = SettingsRepositoryImpl(userPreferences)
    }

    @Test
    fun `getSyncNetworkType should return WIFI_ONLY when preference is true`() = runTest {
        every { userPreferences.isSyncWifiOnly } returns flowOf(true)
        val result = settingsRepositoryImpl.getSyncNetworkType().first()

        assertEquals(SyncNetworkType.WIFI_ONLY, result)
    }

    @Test
    fun `getSyncNetworkType should return ANY_NETWORK when preference is false`() = runTest {
        every { userPreferences.isSyncWifiOnly } returns flowOf(false)
        val result = settingsRepositoryImpl.getSyncNetworkType().first()

        assertEquals(SyncNetworkType.ANY_NETWORK, result)
    }

    @Test
    fun `saveSyncNetworkType should save true when WIFI_ONLY is passed`() = runTest {
        settingsRepositoryImpl.saveSyncNetworkType(SyncNetworkType.WIFI_ONLY)

        coVerify(exactly = 1) { userPreferences.setSyncWifiOnly(true) }
    }
}