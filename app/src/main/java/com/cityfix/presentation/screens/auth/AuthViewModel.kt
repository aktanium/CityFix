package com.cityfix.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val registrationCompleted: Boolean = false,
    val registrationMessage: String? = null
)

sealed interface AuthEvent {
    data class NameChanged(val value: String) : AuthEvent
    data class EmailChanged(val value: String) : AuthEvent
    data class PasswordChanged(val value: String) : AuthEvent
    data object SubmitLogin : AuthEvent
    data object SubmitRegister : AuthEvent
    data object ClearError : AuthEvent
    data object RegistrationNavConsumed : AuthEvent
    data object ClearRegistrationMessage : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(isLoggedIn = isLoggedIn()))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        authRepository.currentUser
            .onEach { user ->
                _uiState.update { it.copy(isLoggedIn = user != null) }
            }
            .launchIn(viewModelScope)
    }

    fun isLoggedIn(): Boolean = authRepository.currentUserId != null

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.NameChanged -> _uiState.update { it.copy(name = event.value, error = null) }
            is AuthEvent.EmailChanged -> _uiState.update { it.copy(email = event.value, error = null) }
            is AuthEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value, error = null) }
            AuthEvent.SubmitLogin -> submitLogin()
            AuthEvent.SubmitRegister -> submitRegister()
            AuthEvent.ClearError -> _uiState.update { it.copy(error = null) }
            AuthEvent.RegistrationNavConsumed -> _uiState.update { it.copy(registrationCompleted = false) }
            AuthEvent.ClearRegistrationMessage -> _uiState.update { it.copy(registrationMessage = null) }
        }
    }

    private fun submitLogin() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Email and password required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            authRepository.signIn(state.email, state.password)
                .onSuccess {
                    _uiState.update {
                        it.copy(isSubmitting = false, password = "", isLoggedIn = true)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, error = e.message ?: "Login failed")
                    }
                }
        }
    }

    private fun submitRegister() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank() || state.name.isBlank()) {
            _uiState.update { it.copy(error = "All fields required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            authRepository.register(state.email, state.password, state.name)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            password = "",
                            isLoggedIn = false,
                            registrationCompleted = true,
                            registrationMessage = "Account created! Please sign in."
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSubmitting = false, error = e.message ?: "Register failed")
                    }
                }
        }
    }
}
