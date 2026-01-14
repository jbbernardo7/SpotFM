package pt.ismai.lastfmlogin.data.repository

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.JsonObject
import pt.ismai.lastfmlogin.data.model.Scrobble
import pt.ismai.lastfmlogin.data.model.UserProfile
import pt.ismai.lastfmlogin.data.network.LastFmApi
import pt.ismai.lastfmlogin.data.network.Supabase
import pt.ismai.lastfmlogin.utils.Constants

class UserRepository(
    private val lastFmApi: LastFmApi,
    private val supabaseClient: SupabaseClient
) {
    suspend fun ensureUserProfileExists(username: String) {
        try {
            // Check if user exists in DB
            Log.d("DEBUG", "Checking if User already exists in Database")
            val existingUser = fetchUserProfileFromDatabase(username)

            // B. If user does NOT exist, fetch from API and Insert
            if (existingUser == null) {
                Log.d("DEBUG", "User does not exist in Supabase, fetching from API")
                fetchAndUpsertUserProfile(username)
            }

        } catch (e: Exception) {
            // Log error, but don't crash the login.
            e.printStackTrace()
        }
    }

    suspend fun fetchAndUpsertUserProfile(username: String) {
        // Call Last.fm API
        val infoResponse = lastFmApi.getUserInfo(
            username = username,
            apiKey = Constants.API_KEY
        )
        val user = infoResponse.user

        // Map to Supabase Model
        // Get the largest image (the last one in the list)
        val largeImage = user.images?.lastOrNull()?.url

        val profile = UserProfile(
            username = user.name,
            real_name = user.realName,
            country = user.country ?: "None",
            playcount = user.playcount?.toIntOrNull() ?: 0,
            image_url = largeImage
        )

        // 3. Insert into Supabase
        supabaseClient.from("user_profiles").upsert(profile) {
            onConflict = "username" // Matches based on Primary Key
        }
    }

    suspend fun fetchUserProfileFromDatabase(username: String): UserProfile? {
        // Fetch the full profile using the data class you created earlier
        Log.d("DEBUG", "Fetching User Data from Database")

        val profile = supabaseClient
            .from("user_profiles")
            .select {
                filter { ilike("username", username) }
            }
            .decodeSingleOrNull<UserProfile>()

        return profile
    }

    suspend fun refreshUserScrobbles(username: String) {
        try {
            // A. Fetch from Last.fm API
            val response = lastFmApi.getRecentTracks(username = username, apiKey = Constants.API_KEY)
            val apiTracks = response.recentTracks.tracks ?: emptyList()

            // B. Map to Database Model
            val scrobblesToInsert = apiTracks.map { track ->
                // Handle "Now Playing" (it has no date, so we use current time or 0)
                val timestamp = track.date?.uts?.toLongOrNull()
                    ?: Long.MAX_VALUE

                Scrobble(
                    username = username,
                    track_name = track.name,
                    artist_name = track.artist.name,
                    album_image = track.images?.lastOrNull()?.url?.takeIf { it.isNotBlank() },
                    date_uts = timestamp
                )
            }

            if (scrobblesToInsert.isNotEmpty()) {
                // C. Delete OLD scrobbles for this user
                supabaseClient.from("scrobbles").delete {
                    filter {
                        ilike("username", username)
                    }
                }

                // D. Insert NEW scrobbles
                supabaseClient.from("scrobbles").insert(scrobblesToInsert)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Re-throw to handle it in ViewModel
        }
    }

    suspend fun getUserScrobbles(username: String): List<Scrobble> {
        return supabaseClient
            .from("scrobbles")
            .select {
                filter { ilike("username", username) }
                // Sort by date descending (Newest first)
                // Note: Since we use Long.MAX_VALUE for "Now Playing", it will naturally be at the top.
                order("date_uts", order = Order.DESCENDING)
            }
            .decodeList<Scrobble>()
    }

    suspend fun updateUserLocation(username: String, lat: Double, long: Double) {
        try {
            // Update the columns we added earlier
            supabaseClient.from("user_profiles").update({
                set("latitude", lat)
                set("longitude", long)
                set("last_active_at", System.currentTimeMillis()) // Mark as online now
            }) {
                filter { eq("username", username) } // Use the canonical username
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 2. Get OTHER users to show on the map
    suspend fun getActiveUsers(currentUsername: String): List<UserProfile> {
        return try {
            // Define "Active" as seen in the last 24 hours (86400000 ms)
            // You can make this shorter (e.g., 10 mins) for a "Real Time" feel
            val activeThreshold = System.currentTimeMillis() - 86400000

            supabaseClient.from("user_profiles").select {
                filter {
                    // Only show users who turned ON visibility
                    eq("is_visible_on_map", true)
                    // Only show recent users
                    //gt("last_active_at", activeThreshold)
                    // Don't fetch myself (I already know where I am)
                    neq("username", currentUsername)
                    // Ensure they actually have a location
                    neq("latitude", -90)
                }
            }.decodeList<UserProfile>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getLastUserScrobble(username: String): Scrobble? {
        return supabaseClient.from("scrobbles")
            .select {
                filter { eq("username", username) }
                // Order by date descending (newest first)
                order("date_uts", order = Order.DESCENDING)
                // limit(1) ensures we only download one row
                limit(1)
            }
            .decodeSingleOrNull<Scrobble>()
    }

    suspend fun updateUserBio(username: String, newBio: String) {
        try {
            supabaseClient.from("user_profiles").update({
                set("bio", newBio)
            }) {
                filter { eq("username", username) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}