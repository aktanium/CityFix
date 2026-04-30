package com.cityfix.domain.repository

import com.cityfix.data.datastore.MapViewPreference
import com.cityfix.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAppSettings(): Flow<AppSettings>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setMapView(preference: MapViewPreference)
    suspend fun setUserName(name: String)
    suspend fun setUserEmail(email: String)
}
