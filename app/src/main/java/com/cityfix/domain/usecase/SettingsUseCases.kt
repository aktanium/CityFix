package com.cityfix.domain.usecase

import com.cityfix.domain.model.AppSettings
import com.cityfix.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = repository.getAppSettings()
}

class UpdateDarkModeUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setDarkMode(enabled)
}

class UpdateNotificationsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setNotificationsEnabled(enabled)
}
