package com.dishut_lampung.sitanihut.domain.usecase.settings

import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import com.dishut_lampung.sitanihut.domain.repository.SettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetSyncNetworkTypeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<SyncNetworkType>{
        return settingsRepository.getSyncNetworkType()
    }
}