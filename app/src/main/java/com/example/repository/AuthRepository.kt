package com.example.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.MySqlDataSource
import com.example.data.UserAccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

data class UserSession(
    val userId: Long,
    val email: String,
    val fullName: String,
    val role: String,
    val isLoggedIn: Boolean
)

sealed class AuthResult {
    data class Success(val user: UserAccountEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(private val context: Context) {
    private val database = com.example.data.ClinicDatabase.getDatabase(context)
    private val userDao = database.userDao()

    private val mySqlSyncRepository = MySqlSyncRepository(context)
    private val mySqlDataSource = com.example.data.MySqlDataSource { mySqlSyncRepository.loadConfig() }

    companion object {
        private val USER_ID = longPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val userSession: Flow<UserSession> = context.dataStore.data.map { prefs ->
        UserSession(
            userId = prefs[USER_ID] ?: -1L,
            email = prefs[USER_EMAIL] ?: "",
            fullName = prefs[USER_NAME] ?: "",
            role = prefs[USER_ROLE] ?: "Staff",
            isLoggedIn = prefs[IS_LOGGED_IN] ?: false
        )
    }

    suspend fun login(email: String, passwordRaw: String): AuthResult {
        val user = userDao.getUserByEmail(email)
        return if (user != null && user.passwordHash == passwordRaw) { // In a real app, use Bcrypt
            saveSession(user)
            AuthResult.Success(user)
        } else {
            AuthResult.Error("Invalid email or password")
        }
    }

    suspend fun register(name: String, email: String, passwordRaw: String): AuthResult {
        if (userDao.getUserByEmail(email) != null) {
            return AuthResult.Error("User with this email already exists")
        }

        val user = UserAccountEntity(
            email = email,
            passwordHash = passwordRaw, // In a real app, hash this
            fullName = name
        )
        val id = userDao.insertUser(user)
        val registeredUser = user.copy(id = id)

        // Best-effort Sync to MySQL
        try {
            val config = mySqlSyncRepository.loadConfig()
            if (config.host.isNotEmpty()) {
                mySqlDataSource.initializeTables()
                mySqlDataSource.registerUser(registeredUser)
            }
        } catch (e: Exception) {
            // Log and ignore remote failure for registration (local is primary)
            android.util.Log.e("AuthRepository", "Remote registration failed: ${e.message}")
        }

        saveSession(registeredUser)
        return AuthResult.Success(registeredUser)
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    private suspend fun saveSession(user: UserAccountEntity) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = user.id
            prefs[USER_EMAIL] = user.email
            prefs[USER_NAME] = user.fullName
            prefs[USER_ROLE] = user.role
            prefs[IS_LOGGED_IN] = true
        }
    }
    
    suspend fun hasUsers(): Boolean {
        return userDao.getUserCount() > 0
    }
}
