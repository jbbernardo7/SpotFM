package pt.ismai.lastfmlogin.data.repository

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
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
}