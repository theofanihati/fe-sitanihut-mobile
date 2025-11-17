package com.dishut_lampung.sitanihut.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }

    suspend fun getAuthToken(): String? {
        val preferences = dataStore.data.first()
        return preferences[AUTH_TOKEN]
    }

    suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
        }
    }

    val hasSeenOnboarding: Flow<Boolean> = dataStore.data
        .map { preferences ->
            // Kembalikan 'false' jika key-nya belum ada
            preferences[HAS_SEEN_ONBOARDING] ?: false
        }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING] = true
        }
    }
}
