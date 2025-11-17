package com.dishut_lampung.sitanihut.presentation.landing_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dishut_lampung.sitanihut.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingPageViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    fun setOnboardingCompleted() {
        //TODO BLMMMM
    }
}