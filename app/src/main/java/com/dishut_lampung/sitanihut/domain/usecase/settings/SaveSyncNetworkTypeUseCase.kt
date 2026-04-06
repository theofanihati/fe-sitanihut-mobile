package com.dishut_lampung.sitanihut.domain.usecase.settings

import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import com.dishut_lampung.sitanihut.domain.repository.SettingsRepository
import jakarta.inject.Inject

class SaveSyncNetworkTypeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(type: SyncNetworkType){
        settingsRepository.saveSyncNetworkType(type)
    }
}