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
import pt.ismai.lastfmlogin.data.network.SupabaseClient

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: UserProfile) : ProfileState()
    object Error : ProfileState()
}

class ProfileViewModel : ViewModel() {
    var state by mutableStateOf<ProfileState>(ProfileState.Loading)
        private set

    fun fetchProfile(username: String) {
        viewModelScope.launch {
            try {
                // Fetch the full profile using the data class you created earlier
                Log.d("DEBUG", "Fetching User Data from Database")
                val profile = SupabaseClient.client
                    .from("user_profiles")
                    .select {
                        filter { eq("username", username) }
                    }
                    .decodeSingleOrNull<UserProfile>()

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
}