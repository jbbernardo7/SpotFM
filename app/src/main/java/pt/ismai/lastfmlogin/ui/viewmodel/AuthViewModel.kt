package pt.ismai.lastfmlogin.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import pt.ismai.lastfmlogin.data.model.LastFmError
import pt.ismai.lastfmlogin.data.model.Session
import pt.ismai.lastfmlogin.data.repository.AuthRepository
import pt.ismai.lastfmlogin.data.repository.UserRepository
import retrofit2.HttpException

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticating : AuthState()
    data class Authenticated(val session: Session) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val authRepository: AuthRepository,private val userRepository: UserRepository) : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var loginState by mutableStateOf<AuthState>(AuthState.Authenticating)
        private set

    init {
        checkLoginStatus()
    }
    fun login() {
        username = username.trim()
        if (username.isBlank() || password.trim().isBlank()) return

        loginState = AuthState.Authenticating

        viewModelScope.launch {
            try {
                val session = authRepository.fetchSessionFromLastFm(username, password)
                authRepository.saveSession(session)

                val canonicalUsername = session.name
                val user = userRepository.fetchUserProfileFromDatabase(canonicalUsername);

                if (user == null) {
                    userRepository.fetchAndUpsertUserProfile(canonicalUsername);
                    userRepository.refreshUserScrobbles(canonicalUsername);
                }
                loginState = AuthState.Authenticated(session)
            } catch (e: Exception) {
                Log.d("DEBUG", "Login Error: ${e.message}")

                val errorMsg = parseErrorMessage(e)
                loginState = AuthState.Error(errorMsg)
            }
        }
    }

    fun checkLoginStatus() {
        viewModelScope.launch {
            val savedSession = authRepository.getSavedSession()
            if (savedSession != null) {
                loginState = AuthState.Authenticated(savedSession)
            } else {
                loginState = AuthState.Unauthenticated
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            username = ""
            password = ""
            loginState = AuthState.Unauthenticated
        }
    }

    private fun parseErrorMessage(e: Exception): String {
        if (e is HttpException) {
            return try {
                val errorBody = e.response()?.errorBody()?.string()
                // Convert JSON to Object
                val errorResponse = Gson().fromJson(errorBody, LastFmError::class.java)
                // Return the clean message, or a fallback
                errorResponse.message ?: "Authentication Failed (Unknown Reason)"
            } catch (parseEx: Exception) {
                "Error parsing server response"
            }
        }
        // Handle standard network errors (Offline, Timeout)
        return e.message ?: "An unknown error occurred"
    }
}