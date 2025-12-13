package com.dishut_lampung.sitanihut.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.worker.DataSyncWorker
import com.dishut_lampung.sitanihut.domain.repository.CommodityRepository
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.repository.ProfileRepository
import com.dishut_lampung.sitanihut.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileState(
    val name: String = "Pengguna",
    val role: String = "User role",
    val imageUrl: String? = null
)

data class MainUiState(
    val isLoading: Boolean = true,
    val startDestination: String = Screen.LandingPage.route
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val homeRepository: HomeRepository,
    private val workManager: WorkManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val userProfileState: StateFlow<UserProfileState> = homeRepository.getUserProfile()
        .map { domainProfile ->
            UserProfileState(
                name = domainProfile.name,
                role = domainProfile.role?.replaceFirstChar { it.uppercase() } ?: "User",
                imageUrl = domainProfile.profilePictureUrl
            )
        }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfileState()
    )

    init {
        checkAuthStatus()
        observeSessionForSync()
    }

    private fun observeSessionForSync() {
        viewModelScope.launch {
            userPreferences.authToken.collect { token ->
                if (!token.isNullOrEmpty()) {
                    Log.d("SYNC_DEBUG", "Token terdeteksi! Menjalankan Worker...")
                    startBackgroundSync()
                }
            }
        }
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val token = userPreferences.authToken.first()
            val role = userPreferences.userRole.first()
            val hasSeenOnboarding = userPreferences.hasSeenOnboarding.first()

            val destination = when {
                !token.isNullOrEmpty() -> {
                    when (role?.lowercase()) {
                        "petani" -> Screen.HomePetani.route
                        "penyuluh" -> Screen.HomePenyuluh.route
                        "penanggung jawab" -> Screen.HomeKkph.route
                        else -> "auth"
                    }
                }
                hasSeenOnboarding -> "auth"
                else -> Screen.LandingPage.route
            }
            _uiState.update {
                it.copy(isLoading = false, startDestination = destination)
            }
        }
    }

    private fun startBackgroundSync() {
        val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("SYNC_WORKER_INITIAL")
            .build()

        // KEEP = klo ada worker yang jalan, tunggu done
        // REPLACE = Cancel yang lama, mulai baru
        workManager.enqueueUniqueWork(
            "initial_data_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        workManager.getWorkInfoByIdLiveData(syncRequest.id).observeForever { workInfo ->
            if (workInfo != null) {
                Log.d("WORKER_STATUS", "Status: ${workInfo.state}")
            }
        }
        Log.d("SYNC_DEBUG", "Request Worker Terkirim ke WorkManager")
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearAllSession()
        }
    }
}