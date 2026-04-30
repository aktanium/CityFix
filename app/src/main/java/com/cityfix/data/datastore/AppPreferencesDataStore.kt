package com.cityfix.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cityfix_preferences")

@Singleton
class AppPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val MAP_VIEW = stringPreferencesKey("map_view")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: false
    }

    val isNotificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    val mapView: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.MAP_VIEW] ?: MapViewPreference.LIST.name
    }

    val userName: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.USER_NAME] ?: "City Resident"
    }

    val userEmail: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.USER_EMAIL] ?: ""
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.DARK_MODE] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setMapView(preference: MapViewPreference) {
        dataStore.edit { prefs -> prefs[Keys.MAP_VIEW] = preference.name }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { prefs -> prefs[Keys.USER_NAME] = name }
    }

    suspend fun setUserEmail(email: String) {
        dataStore.edit { prefs -> prefs[Keys.USER_EMAIL] = email }
    }
}

enum class MapViewPreference { LIST, MAP }
