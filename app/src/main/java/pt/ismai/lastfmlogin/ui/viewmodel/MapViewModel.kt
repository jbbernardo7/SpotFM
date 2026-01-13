package pt.ismai.lastfmlogin.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.geojson.Point
import kotlinx.coroutines.launch
import pt.ismai.lastfmlogin.data.model.UserProfile
import pt.ismai.lastfmlogin.data.repository.LocationRepository
import pt.ismai.lastfmlogin.data.repository.UserRepository

// State to hold map data
data class MapState(
    val userLocation: Point? = null, // My location (MapBox uses 'Point')
    val nearbyUsers: List<UserProfile> = emptyList(), // Other users
    val isLoading: Boolean = false
)

class MapViewModel(
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    var state by mutableStateOf(MapState())
        private set


    fun initialize(username: String) {
        viewModelScope.launch {
            /*
            // 1. Fetch my current visibility settings
            val myProfile = userRepository.fetchUserProfileFromDatabase(username)
            state = state.copy(isVisibleOnMap = myProfile?.is_visible_on_map ?: false)

            */
            // 2. Start Location Logic
            getCurrentLocation(username)
        }
    }

    fun getCurrentLocation(username: String) {
        Log.d("MAP", "FETCHING LOCATION FOR: {$username}")
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            // 1. Get Location cleanly from Repository
            val location = locationRepository.getCurrentLocation()

            if (location != null) {
                val lat = location.latitude
                val long = location.longitude

                // Update UI State
                state = state.copy(
                    userLocation = Point.fromLngLat(long, lat),
                    isLoading = false
                )

                // 2. Sync with Supabase
                // A. Save my location
                // Wrap network calls in try-catch
                try {
                    userRepository.updateUserLocation(username, lat, long)
                    val users = userRepository.getActiveUsers(username)
                    Log.d("MAP", "FETCHED USERS: {$users}")
                    state = state.copy(nearbyUsers = users)
                } catch (e: Exception) {
                    // Handle network error (show snackbar, retry, etc.)
                    Log.e("MapViewModel", "Failed to sync location", e)
                }
            } else {
                state = state.copy(isLoading = false)
            }
        }
    }

    /*fun toggleVisibility(username: String, isVisible: Boolean) {
        viewModelScope.launch {
            state = state.copy(isVisibleOnMap = isVisible)
            // Update DB
            // Note: You might need to add a specific function in UserRepository for just this column,
            // or use a generic update. For now assuming we have a way to update it.
            userRepository.toggleMapVisibility(username, isVisible)

            // If turning OFF, maybe clear location from DB? (Optional privacy feature)
        }
    }*/

    suspend fun getLastTrack(username: String): pt.ismai.lastfmlogin.data.model.Scrobble? {
        return try {
            userRepository.refreshUserScrobbles(username);
            val history = userRepository.getUserScrobbles(username)
            history.firstOrNull() // The first item is usually the most recent
        } catch (e: Exception) {
            null
        }
    }
}