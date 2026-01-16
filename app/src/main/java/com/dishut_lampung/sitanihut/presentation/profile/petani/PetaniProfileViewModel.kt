package com.dishut_lampung.sitanihut.presentation.profile.petani

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import com.dishut_lampung.sitanihut.domain.usecase.profile.GetMyProfileUseCase
import com.dishut_lampung.sitanihut.presentation.profile.ProfileState
import com.dishut_lampung.sitanihut.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetaniProfileViewModel @Inject constructor(
    private val getUserDetailUseCase: GetMyProfileUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        getProfile()
    }

    fun getProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, generalError = null) }

            val userId = userPreferences.userId.first()

            if (userId.isNullOrBlank()) {
                _state.update {
                    it.copy(isLoading = false, generalError = "hmmm, datamu gaada nih")
                }
                return@launch
            }

            getUserDetailUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.update { it.copy(isLoading = true, generalError = null) }
                    }
                    is Resource.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                user = result.data,
                                generalError = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                user = result.data ?: it.user,
                                generalError = result.message ?: "Terjadi kesalahan"
                            )
                        }
                    }
                }
            }
        }
    }
}