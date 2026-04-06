package com.dishut_lampung.sitanihut.domain.repository

import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSyncNetworkType(): Flow<SyncNetworkType>
    suspend fun saveSyncNetworkType(type: SyncNetworkType)
}