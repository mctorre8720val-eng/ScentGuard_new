package com.example.scentguard.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scentguard_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        
        // Session Keys
        val SESSION_UID = stringPreferencesKey("session_uid")
        val SESSION_ROLE = stringPreferencesKey("session_role")
        val SESSION_RESTAURANT_ID = stringPreferencesKey("session_restaurant_id")
        val SELECTED_ALARM_SOUND_ID = stringPreferencesKey("selected_alarm_sound_id")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED] = completed
        }
    }

    // Session Management
    val sessionUid: Flow<String?> = context.dataStore.data.map { it[SESSION_UID] }
    val sessionRole: Flow<String?> = context.dataStore.data.map { it[SESSION_ROLE] }
    val sessionRestaurantId: Flow<String?> = context.dataStore.data.map { it[SESSION_RESTAURANT_ID] }

    val selectedAlarmSoundId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_ALARM_SOUND_ID] ?: "critical_alarm"
    }

    suspend fun setAlarmSoundId(soundId: String) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_ALARM_SOUND_ID] = soundId
        }
    }

    suspend fun saveSession(uid: String, role: String, restaurantId: String) {
        context.dataStore.edit { prefs ->
            prefs[SESSION_UID] = uid
            prefs[SESSION_ROLE] = role
            prefs[SESSION_RESTAURANT_ID] = restaurantId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(SESSION_UID)
            prefs.remove(SESSION_ROLE)
            prefs.remove(SESSION_RESTAURANT_ID)
        }
    }
}
