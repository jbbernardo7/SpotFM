package pt.ismai.lastfmlogin.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import pt.ismai.lastfmlogin.data.model.UserProfile
import pt.ismai.lastfmlogin.data.network.Supabase
import pt.ismai.lastfmlogin.data.repository.AuthRepository
import pt.ismai.lastfmlogin.data.repository.UserRepository

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: UserProfile) : ProfileState()
    object Error : ProfileState()
}

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {
    var state by mutableStateOf<ProfileState>(ProfileState.Loading)
        private set

    fun fetchProfile(username: String) {
        viewModelScope.launch {
            try {
                val profile = repository.fetchUserProfileFromDatabase(username)

                state = if (profile != null) {
                    ProfileState.Success(profile)
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

                // 2. Reload the UI with the new data from DB
                fetchProfile(username)
            } catch (e: Exception) {
                // Handle error (maybe show a Snackbar)
                e.printStackTrace()
            }
        }
    }
}