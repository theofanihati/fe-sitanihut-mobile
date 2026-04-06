package com.dishut_lampung.sitanihut.data.repository

import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import com.dishut_lampung.sitanihut.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
): SettingsRepository {
    override fun getSyncNetworkType(): Flow<SyncNetworkType> {
        return userPreferences.isSyncWifiOnly.map { isWifiOnly ->
            if (isWifiOnly) {
                SyncNetworkType.WIFI_ONLY
            } else {
                SyncNetworkType.ANY_NETWORK
            }
        }
    }

    override suspend fun saveSyncNetworkType(type: SyncNetworkType) {
        val isWifiOnly = type == SyncNetworkType.WIFI_ONLY
        userPreferences.setSyncWifiOnly(isWifiOnly)
    }
}