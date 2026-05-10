package com.cityfix.data.repository

import com.cityfix.data.datastore.AppPreferencesDataStore
import com.cityfix.data.datastore.MapViewPreference
import com.cityfix.domain.model.AppSettings
import com.cityfix.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: AppPreferencesDataStore
) : SettingsRepository {

    override fun getAppSettings(): Flow<AppSettings> = combine(
        dataStore.isDarkMode,
        dataStore.isNotificationsEnabled,
        dataStore.mapView
    ) { darkMode, notifications, mapView ->
        AppSettings(
            isDarkMode = darkMode,
            isNotificationsEnabled = notifications,
            mapView = mapView
        )
    }

    override suspend fun setDarkMode(enabled: Boolean) =
        dataStore.setDarkMode(enabled)

    override suspend fun setNotificationsEnabled(enabled: Boolean) =
        dataStore.setNotificationsEnabled(enabled)

    override suspend fun setMapView(preference: MapViewPreference) =
        dataStore.setMapView(preference)

    override suspend fun getLastSearch(): String =
        dataStore.lastSearch.first()

    override suspend fun saveLastSearch(query: String) =
        dataStore.setLastSearch(query)
}
