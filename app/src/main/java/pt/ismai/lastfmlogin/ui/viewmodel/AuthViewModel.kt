package pt.ismai.lastfmlogin.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ismai.lastfmlogin.data.model.Session
import pt.ismai.lastfmlogin.data.repository.AuthRepository

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticating : AuthState()
    data class Authenticated(val session: Session) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var loginState by mutableStateOf<AuthState>(AuthState.Authenticating)
        private set

    init {
        checkLoginStatus()
    }
    fun login() {
        if (username.trim().isBlank() || password.trim().isBlank()) return

        loginState = AuthState.Authenticating

        viewModelScope.launch {
            try {
                val session = repository.loginAndSaveSession(username, password)
                loginState = AuthState.Authenticated(session)
            } catch (e: Exception) {
                loginState = AuthState.Error(e.message ?: "Login Error")
            }
        }
    }

    fun checkLoginStatus() {
        viewModelScope.launch {
            val savedSession = repository.getSavedSession()
            if (savedSession != null) {
                loginState = AuthState.Authenticated(savedSession)
            } else {
                loginState = AuthState.Unauthenticated
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            username = ""
            password = ""
            loginState = AuthState.Unauthenticated
        }
    }
}