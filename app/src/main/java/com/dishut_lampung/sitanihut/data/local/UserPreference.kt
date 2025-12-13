package com.dishut_lampung.sitanihut.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_AVATAR = stringPreferencesKey("user_avatar")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    // token
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }

    suspend fun getAuthToken(): String? {
        val preferences = dataStore.data.first()
        return preferences[AUTH_TOKEN]
    }

    val authToken: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN]
        }

    // role
    val userRole: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[USER_ROLE]
        }

    suspend fun saveUserRole(role: String) {
        dataStore.edit { preferences ->
            preferences[USER_ROLE] = role
        }
    }

    // name
    val userName: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[USER_NAME] ?: "Pengguna"
        }

    suspend fun saveUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    // id
    suspend fun saveUserId(id: String) {
        dataStore.edit { it[USER_ID] = id }
    }

    val userId: Flow<String?> = dataStore.data.map { it[USER_ID] }

    // profpic
    suspend fun saveUserAvatar(url: String) {
        dataStore.edit { it[USER_AVATAR] = url }
    }

    val userAvatar: Flow<String?> = dataStore.data.map { it[USER_AVATAR] }

    val lastSyncTime: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIME] ?: 0L
    }

    suspend fun updateLastSyncTime() {
        val currentTime = System.currentTimeMillis()
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = currentTime
        }
    }

    suspend fun clearAllSession() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(USER_ROLE)
            preferences.remove(USER_NAME)
            preferences.remove(USER_ID)
            preferences.remove(USER_AVATAR)
        }
    }

    suspend fun clearAuthToken() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
        }
    }

    suspend fun clearUserRole() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ROLE)
        }
    }

    val hasSeenOnboarding: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[HAS_SEEN_ONBOARDING] ?: false
        }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING] = true
        }
    }
}
