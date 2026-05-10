package com.cityfix.presentation.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cityfix.presentation.theme.BrandPrimary

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onEvent: (AuthEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onRegistrationComplete: () -> Unit
) {
    LaunchedEffect(uiState.registrationCompleted) {
        if (uiState.registrationCompleted) {
            onRegistrationComplete()
            onEvent(AuthEvent.RegistrationNavConsumed)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrandHeader()

            Spacer(modifier = Modifier.height(40.dp))

            BrandedTextField(
                value = uiState.name,
                onValueChange = { onEvent(AuthEvent.NameChanged(it)) },
                label = "Name",
                leadingIcon = Icons.Filled.Person
            )

            Spacer(modifier = Modifier.height(12.dp))

            BrandedTextField(
                value = uiState.email,
                onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
                label = "Email",
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(12.dp))

            PasswordTextField(
                value = uiState.password,
                onValueChange = { onEvent(AuthEvent.PasswordChanged(it)) },
                leadingIcon = Icons.Filled.Lock,
                error = uiState.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            BrandPrimaryButton(
                text = "Create account",
                onClick = { onEvent(AuthEvent.SubmitRegister) },
                isLoading = uiState.isSubmitting
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onNavigateBack) {
                Text("Already have an account? Sign in", color = BrandPrimary)
            }
        }
    }
}
