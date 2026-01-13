package pt.ismai.lastfmlogin.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class LocationRepository(context: Context) {

    // The client is created here, using the context passed by AppContainer
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Suspend function that returns the Location or null.
     * We use 'await()' to turn the Google Play Services 'Task' into a Kotlin Coroutine.
     */
    @SuppressLint("MissingPermission") // Permissions are checked in the UI before calling this
    suspend fun getCurrentLocation(): Location? {
        return try {
            // Priority.PRIORITY_HIGH_ACCURACY ensures we get GPS data
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}