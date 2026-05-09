package com.cityfix.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.model.ReportStatus
import com.cityfix.domain.repository.AuthRepository
import com.cityfix.domain.usecase.GetAllReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ProfileUiState(
    val userName: String = "",
    val userEmail: String = "",
    val totalReports: Int = 0,
    val newReports: Int = 0,
    val inProgressReports: Int = 0,
    val resolvedReports: Int = 0,
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null
)

sealed interface ProfileEvent {
    data object DismissSnackbar : ProfileEvent
    data object Logout : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getAllReportsUseCase: GetAllReportsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.DismissSnackbar -> _uiState.update { it.copy(snackbarMessage = null) }
            ProfileEvent.Logout -> {} // Handled by AuthViewModel in UI
        }
    }

    private fun loadData() {
        combine(
            authRepository.currentUser,
            getAllReportsUseCase()
        ) { firebaseUser, reports ->
            val email = firebaseUser?.email.orEmpty()
            val displayName = firebaseUser?.displayName
            val resolvedName = displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore('@').takeIf { it.isNotBlank() }
                ?: ""

            _uiState.update {
                it.copy(
                    userName = resolvedName,
                    userEmail = email,
                    totalReports = reports.size,
                    newReports = reports.count { r -> r.status == ReportStatus.NEW.name },
                    inProgressReports = reports.count { r -> r.status == ReportStatus.IN_PROGRESS.name },
                    resolvedReports = reports.count { r -> r.status == ReportStatus.RESOLVED.name },
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }
}
