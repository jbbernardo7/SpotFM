package pt.ismai.lastfmlogin.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ismai.lastfmlogin.data.model.UserProfile
import pt.ismai.lastfmlogin.data.repository.LocationRepository
import pt.ismai.lastfmlogin.data.repository.UserRepository
import kotlin.math.*

// Wrapper class to hold the user and their calculated distance
data class NearbyUser(
    val user: UserProfile,
    val distanceKm: Double
)

sealed class NearbyState {
    object Loading : NearbyState()
    data class Success(val users: List<NearbyUser>) : NearbyState()
    object Error : NearbyState()
    object Empty : NearbyState() // No one found within 10km
}

class NearbyViewModel(
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    var state by mutableStateOf<NearbyState>(NearbyState.Loading)
        private set

    fun loadNearbyUsers(myUsername: String, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            state = NearbyState.Loading
            try {
                // 1. Get MY current location
                val myLoc = locationRepository.getCurrentLocation()

                if (myLoc == null) {
                    state = NearbyState.Error
                    return@launch
                }

                // 2. Get ALL active users from DB (reusing your existing function)
                val allActiveUsers = userRepository.getActiveUsers(myUsername)

                // 3. Filter and Sort by Distance
                val nearbyList = allActiveUsers.mapNotNull { user ->
                    if (user.latitude != null && user.longitude != null) {
                        val dist = calculateDistance(
                            myLoc.latitude, myLoc.longitude,
                            user.latitude, user.longitude
                        )
                        if (dist <= radiusKm) {
                            NearbyUser(user, dist)
                        } else null
                    } else null
                }.sortedBy { it.distanceKm } // Closest first

                // 4. Update State
                state = if (nearbyList.isEmpty()) {
                    NearbyState.Empty
                } else {
                    NearbyState.Success(nearbyList)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                state = NearbyState.Error
            }
        }
    }

    // --- Helper: Haversine Formula ---
    // Calculates distance in km between two lat/long points
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}