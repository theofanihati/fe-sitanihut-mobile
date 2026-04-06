package com.dishut_lampung.sitanihut.domain.usecase.settings

import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import com.dishut_lampung.sitanihut.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetSyncNetworkTypeUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var getSyncNetworkTypeUseCase: GetSyncNetworkTypeUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        getSyncNetworkTypeUseCase = GetSyncNetworkTypeUseCase(settingsRepository)
    }

    @Test
    fun `invoke should return sync network type from repository`() = runTest {
        val expectedType = SyncNetworkType.WIFI_ONLY
        every { settingsRepository.getSyncNetworkType() } returns flowOf(expectedType)

        val result = getSyncNetworkTypeUseCase().first()
        assertEquals(expectedType, result)
        verify(exactly = 1) { settingsRepository.getSyncNetworkType() }
    }
}