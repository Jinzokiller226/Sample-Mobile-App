package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.repository.AuthRepository
import com.example.repository.AuthResult
import com.example.repository.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val session: UserSession) : AuthState()
    object Unauthenticated : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)

    val authState: StateFlow<AuthState> = authRepository.userSession
        .map { session ->
            if (session.isLoggedIn) AuthState.Authenticated(session)
            else AuthState.Unauthenticated
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Loading)
    
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun login(email: String, passwordRaw: String) {
        viewModelScope.launch {
            _authError.value = null
            when (val result = authRepository.login(email, passwordRaw)) {
                is AuthResult.Success -> { /* Session updated via DataStore */ }
                is AuthResult.Error -> _authError.value = result.message
            }
        }
    }

    fun register(name: String, email: String, passwordRaw: String) {
        viewModelScope.launch {
            _authError.value = null
            when (val result = authRepository.register(name, email, passwordRaw)) {
                is AuthResult.Success -> { /* Session updated via DataStore */ }
                is AuthResult.Error -> _authError.value = result.message
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _authError.value = null
    }
}
