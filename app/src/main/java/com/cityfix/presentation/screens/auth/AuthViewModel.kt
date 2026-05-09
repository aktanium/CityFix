package com.cityfix.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cityfix.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.cityfix.data.datastore.AppPreferencesDataStore
import javax.inject.Inject

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null
)

sealed interface AuthEvent {
    data class NameChanged(val value: String) : AuthEvent
    data class EmailChanged(val value: String) : AuthEvent
    data class PasswordChanged(val value: String) : AuthEvent
    data object SubmitLogin : AuthEvent
    data object SubmitRegister : AuthEvent
    data object ClearError : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val preferences: AppPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isLoggedIn = isLoggedIn()) }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            preferences.clearAll()
            _uiState.update { it.copy(isLoggedIn = false) }
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
            try {
                auth.signInWithEmailAndPassword(state.email, state.password).await()
                _uiState.update { it.copy(isSubmitting = false, password = "", isLoggedIn = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Login failed") }
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
            try {
                val result = auth.createUserWithEmailAndPassword(state.email, state.password).await()
                val trimmedName = state.name.trim()
                if (trimmedName.isNotEmpty()) {
                    result.user?.updateProfile(
                        userProfileChangeRequest { displayName = trimmedName }
                    )?.await()
                }
                _uiState.update { it.copy(isSubmitting = false, password = "", isLoggedIn = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.message ?: "Register failed") }
            }
        }
    }
}
