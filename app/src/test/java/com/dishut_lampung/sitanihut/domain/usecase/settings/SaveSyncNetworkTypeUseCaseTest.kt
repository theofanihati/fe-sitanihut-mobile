package com.dishut_lampung.sitanihut.domain.usecase.settings

import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import com.dishut_lampung.sitanihut.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveSyncNetworkTypeUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var saveSyncNetworkTypeUseCase: SaveSyncNetworkTypeUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        saveSyncNetworkTypeUseCase = SaveSyncNetworkTypeUseCase(settingsRepository)
    }

    @Test
    fun `invoke should call saveSyncNetworkType on repository`() = runTest {
        val typeToSave = SyncNetworkType.WIFI_ONLY
        coEvery { settingsRepository.saveSyncNetworkType(typeToSave) } returns Unit

        saveSyncNetworkTypeUseCase(typeToSave)
        coVerify(exactly = 1) { settingsRepository.saveSyncNetworkType(typeToSave) }
    }
}