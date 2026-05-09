package com.cityfix.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    val currentUserId: String?

    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String, name: String): Result<Unit>
    suspend fun signOut()
}
