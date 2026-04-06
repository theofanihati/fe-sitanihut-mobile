package com.dishut_lampung.sitanihut.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.data.worker.DataSyncWorker
import com.dishut_lampung.sitanihut.domain.model.SyncNetworkType
import com.dishut_lampung.sitanihut.domain.repository.CommodityRepository
import com.dishut_lampung.sitanihut.domain.repository.HomeRepository
import com.dishut_lampung.sitanihut.domain.repository.ProfileRepository
import com.dishut_lampung.sitanihut.domain.repository.UserRepository
import com.dishut_lampung.sitanihut.domain.usecase.auth.LogoutUseCase
import com.dishut_lampung.sitanihut.domain.usecase.settings.GetSyncNetworkTypeUseCase
import com.dishut_lampung.sitanihut.presentation.shared.navigation.Screen
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import java.util.concurrent.TimeUnit
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
    private val workManager: WorkManager,
    private val userRepository: UserRepository,
    private val logoutUseCase: LogoutUseCase,
    private val getSyncNetworkTypeUseCase: GetSyncNetworkTypeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    companion object {
        private const val SYNC_COOLDOWN_MS = 30 * 60 * 1000L
        private const val SYNC_TAG_PERIODIC = "periodic_data_sync"
        private const val SYNC_TAG_INITIAL = "initial_data_sync"
    }

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
            combine(
                userPreferences.authToken,
                userPreferences.lastSyncTime,
                getSyncNetworkTypeUseCase()
            ) { token, lastSync, networkType ->
                Triple(token, lastSync, networkType)
            }.collect { (token, lastSync, networkType) ->
                if (!token.isNullOrEmpty()) {
                    val currentTime = System.currentTimeMillis()
                    val timeDiff = currentTime - lastSync

                    if (timeDiff > SYNC_COOLDOWN_MS) {
                        Log.d("SYNC", "Data sudah basi (${timeDiff/60000} menit lalu). Mulai Sync...")
                        startBackgroundSync(networkType)
                    } else {
                        Log.d("SYNC", "Data masih segar. Skip Sync. Tunggu ${(SYNC_COOLDOWN_MS - timeDiff)/60000} menit lagi.")
                    }

                    setupPeriodicSync(networkType)
                    syncCurrentFcmToken()
                }
            }
        }
    }

    private fun setupPeriodicSync(networkType: SyncNetworkType) {
        val requiredNetwork = if (networkType == SyncNetworkType.WIFI_ONLY) {
            NetworkType.UNMETERED
        } else {
            NetworkType.CONNECTED
        }

        val periodicRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            12, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(requiredNetwork)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_TAG_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
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

    private fun syncCurrentFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
//                Log.w("FCM", "Gagal ambil token lokal", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
//            Log.d("FCM", "Token FCM didapat: $token")

            viewModelScope.launch {
                try {
                    userRepository.syncFcmToken(token)
//                    Log.d("FCM", "Sukses kirim token ke Backend!")
                } catch (e: Exception) {
//                    Log.e("FCM", "Gagal kirim token ke Backend: ${e.message}")
                }
            }
        }
    }

    private fun startBackgroundSync(networkType: SyncNetworkType) {
        val requiredNetwork = if (networkType == SyncNetworkType.WIFI_ONLY) {
            NetworkType.UNMETERED
        } else {
            NetworkType.CONNECTED
        }

        val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(requiredNetwork)
                    .build()
            )
            .addTag("SYNC_WORKER_INITIAL")
            .build()

        // KEEP = klo ada worker yang jalan, tunggu done
        // REPLACE = Cancel yang lama, mulai baru
        workManager.enqueueUniqueWork(
            SYNC_TAG_INITIAL,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            logoutUseCase()
        }
    }
}