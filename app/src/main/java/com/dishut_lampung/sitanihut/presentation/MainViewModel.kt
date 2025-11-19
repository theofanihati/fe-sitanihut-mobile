package com.dishut_lampung.sitanihut.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileState(
    val name: String = "Pengguna",
    val role: String = "User role"
)

data class MainUiState(
    val isLoading: Boolean = true,
    val startDestination: String = Screen.LandingPage.route
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val userProfileState: StateFlow<UserProfileState> = combine(
        userPreferences.userName,
        userPreferences.userRole
    ) { name, role ->
        UserProfileState(
            name = name ?: "Pengguna",
            role = role?.replaceFirstChar { it.uppercase() } ?: "User"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfileState()
    )

    init {
        viewModelScope.launch {
                val token = userPreferences.authToken.first()
                val role = userPreferences.userRole.first()
                val hasSeenOnboarding = userPreferences.hasSeenOnboarding.first()

                val destination = when {
                    !token.isNullOrEmpty() -> {
                        when (role?.lowercase()) {
                            "petani" -> Screen.HomePetani.route
                            "penyuluh" -> Screen.HomePenyuluh.route
                            "kkph" -> Screen.HomeKkph.route
                            else -> "auth"
                        }
                    }
                    hasSeenOnboarding -> "auth"
                    else -> Screen.LandingPage.route
                }
                _uiState.value = MainUiState(
                    isLoading = false,
                    startDestination = destination
                )

        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearAllSession()
        }
    }
}