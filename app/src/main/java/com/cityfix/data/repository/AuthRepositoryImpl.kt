package com.cityfix.data.repository

import com.cityfix.data.datastore.AppPreferencesDataStore
import com.cityfix.data.local.dao.UserDao
import com.cityfix.data.local.entity.UserEntity
import com.cityfix.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val preferences: AppPreferencesDataStore
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = preferences.isLoggedIn
    override val currentUserId: Flow<Long?> = preferences.userId

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val normalizedEmail = email.trim().lowercase(Locale.US)
        require(normalizedEmail.isNotBlank()) { "Email is required" }
        require(password.isNotBlank()) { "Password is required" }

        val user = userDao.getUserByEmail(normalizedEmail)
            ?: error("Invalid email or password")

        val expectedHash = hashPassword(password, user.passwordSalt)
        if (expectedHash != user.passwordHash) {
            error("Invalid email or password")
        }

        preferences.setLoggedIn(true)
        preferences.setSessionEmail(normalizedEmail)
        preferences.setUserId(user.id)
        preferences.setUserName(user.name)
        preferences.setUserEmail(user.email)
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<Unit> = runCatching {
        val trimmedName = name.trim()
        val normalizedEmail = email.trim().lowercase(Locale.US)

        require(trimmedName.isNotBlank()) { "Name is required" }
        require(normalizedEmail.isNotBlank()) { "Email is required" }
        require(password.length >= 6) { "Password must be at least 6 characters" }

        val existing = userDao.getUserByEmail(normalizedEmail)
        require(existing == null) { "User already exists" }

        val salt = generateSalt()
        val hash = hashPassword(password, salt)

        val newUserId = userDao.insertUser(
            UserEntity(
                name = trimmedName,
                email = normalizedEmail,
                passwordHash = hash,
                passwordSalt = salt,
                createdAt = System.currentTimeMillis()
            )
        )

        preferences.setLoggedIn(true)
        preferences.setSessionEmail(normalizedEmail)
        preferences.setUserId(newUserId)
        preferences.setUserName(trimmedName)
        preferences.setUserEmail(normalizedEmail)
    }

    override suspend fun logout() {
        preferences.setLoggedIn(false)
        preferences.setSessionEmail(null)
        preferences.setUserId(null)
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    private fun hashPassword(password: String, saltHex: String): String {
        val bytes = (password + ":" + saltHex).toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.toHex()
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { b -> "%02x".format(b) }
}
