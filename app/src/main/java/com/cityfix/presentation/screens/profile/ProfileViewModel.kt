package com.cityfix.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.model.AppSettings
import com.cityfix.domain.model.ReportStatus
import com.cityfix.domain.usecase.GetAllReportsUseCase
import com.cityfix.domain.usecase.GetAppSettingsUseCase
import com.cityfix.domain.usecase.UpdateUserProfileUseCase
import com.cityfix.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userName: String = "",
    val userEmail: String = "",
    val totalReports: Int = 0,
    val newReports: Int = 0,
    val inProgressReports: Int = 0,
    val resolvedReports: Int = 0,
    val isLoading: Boolean = true,
    val isEditingProfile: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val snackbarMessage: String? = null
)

sealed interface ProfileEvent {
    data object StartEditProfile : ProfileEvent
    data object CancelEditProfile : ProfileEvent
    data object SaveProfile : ProfileEvent
    data class EditNameChanged(val name: String) : ProfileEvent
    data class EditEmailChanged(val email: String) : ProfileEvent
    data object DismissSnackbar : ProfileEvent
    data object Logout : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val getAllReportsUseCase: GetAllReportsUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.StartEditProfile -> _uiState.update {
                it.copy(
                    isEditingProfile = true,
                    editName = it.userName,
                    editEmail = it.userEmail
                )
            }
            ProfileEvent.CancelEditProfile -> _uiState.update { it.copy(isEditingProfile = false) }
            ProfileEvent.SaveProfile -> saveProfile()
            is ProfileEvent.EditNameChanged -> _uiState.update { it.copy(editName = event.name) }
            is ProfileEvent.EditEmailChanged -> _uiState.update { it.copy(editEmail = event.email) }
            ProfileEvent.DismissSnackbar -> _uiState.update { it.copy(snackbarMessage = null) }
            ProfileEvent.Logout -> {} // Handled by AuthViewModel in UI
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            val email = firebaseUser?.email.orEmpty()
            val displayName = firebaseUser?.displayName
            val resolvedName = displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore('@').takeIf { it.isNotBlank() }
                ?: ""

            combine(
                getAppSettingsUseCase(),
                getAllReportsUseCase()
            ) { settings, reports ->
                _uiState.update {
                    it.copy(
                        userName = resolvedName,
                        userEmail = email,
                        totalReports = reports.size,
                        newReports = reports.count { r -> r.status == com.cityfix.domain.model.ReportStatus.NEW.name },
                        inProgressReports = reports.count { r -> r.status == com.cityfix.domain.model.ReportStatus.IN_PROGRESS.name },
                        resolvedReports = reports.count { r -> r.status == com.cityfix.domain.model.ReportStatus.RESOLVED.name },
                        isLoading = false
                    )
                }
            }.collect()
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            updateUserProfileUseCase(state.editName.trim(), state.editEmail.trim())
            _uiState.update {
                it.copy(
                    isEditingProfile = false,
                    snackbarMessage = "Profile updated successfully"
                )
            }
        }
    }
}
