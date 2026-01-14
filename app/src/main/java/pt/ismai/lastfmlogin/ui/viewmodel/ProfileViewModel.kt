package pt.ismai.lastfmlogin.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import pt.ismai.lastfmlogin.data.model.Scrobble
import pt.ismai.lastfmlogin.data.model.UserProfile
import pt.ismai.lastfmlogin.data.network.Supabase
import pt.ismai.lastfmlogin.data.repository.AuthRepository
import pt.ismai.lastfmlogin.data.repository.UserRepository

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(
        val profile: UserProfile,
        val scrobbles: List<Scrobble> // <--- Added this
    ) : ProfileState()
    object Error : ProfileState()
}

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {
    var state by mutableStateOf<ProfileState>(ProfileState.Loading)
        private set

    fun fetchProfile(username: String) {
        viewModelScope.launch {
            try {
                val profile = repository.fetchUserProfileFromDatabase(username)
                val scrobbles = try {
                    repository.getUserScrobbles(username)
                } catch (e: Exception) {
                    emptyList()
                }

                state = if (profile != null) {
                    ProfileState.Success(profile, scrobbles)
                } else {
                    ProfileState.Error
                }
            } catch (e: Exception) {
                e.printStackTrace()
                state = ProfileState.Error
            }
        }
    }

    fun refreshProfile(username: String) {
        viewModelScope.launch {
            state = ProfileState.Loading

            try {
                // 1. Force API Update
                repository.fetchAndUpsertUserProfile(username)
                repository.refreshUserScrobbles(username)

                // 2. Reload the UI with the new data from DB
                fetchProfile(username)
            } catch (e: Exception) {
                // Handle error (maybe show a Snackbar)
                e.printStackTrace()
                // If refresh fails, try to just load what we have
                fetchProfile(username)
            }
        }
    }

    fun updateBio(username: String, newBio: String) {
        viewModelScope.launch {
            // 1. Optimistic Update (Update UI immediately)
            val currentState = state
            if (currentState is ProfileState.Success) {
                state = currentState.copy(
                    profile = currentState.profile.copy(bio = newBio)
                )
            }

            // 2. Save to Database
            try {
                repository.updateUserBio(username, newBio)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}