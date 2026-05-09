package com.cityfix.data.repository

import com.cityfix.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { state ->
            trySend(state.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        Unit
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String
    ): Result<Unit> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val trimmedName = name.trim()
        if (trimmedName.isNotEmpty()) {
            result.user?.updateProfile(
                userProfileChangeRequest { displayName = trimmedName }
            )?.await()
        }
        // Don't auto-login on register — force the user to sign in explicitly.
        auth.signOut()
        Unit
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
