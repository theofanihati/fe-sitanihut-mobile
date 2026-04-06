package com.dishut_lampung.sitanihut.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import com.dishut_lampung.sitanihut.domain.usecase.settings.GetSyncNetworkTypeUseCase
import com.dishut_lampung.sitanihut.domain.usecase.settings.SaveSyncNetworkTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getSyncNetworkTypeUseCase: GetSyncNetworkTypeUseCase,
    private val saveSyncNetworkTypeUseCase: SaveSyncNetworkTypeUseCase
) : ViewModel() {
    val networkPreference: StateFlow<SyncNetworkType> = getSyncNetworkTypeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncNetworkType.ANY_NETWORK
        )

    fun onNetworkPreferenceChanged(newType: SyncNetworkType) {
        viewModelScope.launch {
            saveSyncNetworkTypeUseCase(newType)
        }
    }
}