package com.cityfix.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.model.AppSettings
import com.cityfix.domain.usecase.GetAppSettingsUseCase
import com.cityfix.domain.usecase.UpdateDarkModeUseCase
import com.cityfix.domain.usecase.UpdateNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: AppSettings? = null,
    val isLoading: Boolean = true
)

sealed interface SettingsEvent {
    data class ToggleDarkMode(val enabled: Boolean) : SettingsEvent
    data class ToggleNotifications(val enabled: Boolean) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val updateDarkModeUseCase: UpdateDarkModeUseCase,
    private val updateNotificationsUseCase: UpdateNotificationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAppSettingsUseCase()
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect { settings ->
                    _uiState.update { it.copy(settings = settings, isLoading = false) }
                }
        }
    }

    fun onEvent(event: SettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsEvent.ToggleDarkMode -> updateDarkModeUseCase(event.enabled)
                is SettingsEvent.ToggleNotifications -> updateNotificationsUseCase(event.enabled)
            }
        }
    }
}
