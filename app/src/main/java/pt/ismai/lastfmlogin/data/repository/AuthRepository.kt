package pt.ismai.lastfmlogin.data.repository

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import pt.ismai.lastfmlogin.data.local.SessionManager
import pt.ismai.lastfmlogin.data.model.Session
import pt.ismai.lastfmlogin.data.model.UserProfile
import pt.ismai.lastfmlogin.data.network.LastFmApi
import pt.ismai.lastfmlogin.data.network.SupabaseClient
import pt.ismai.lastfmlogin.utils.Constants
import pt.ismai.lastfmlogin.utils.CryptUtils

class AuthRepository(private val lastFmApi: LastFmApi, private val sessionManager: SessionManager) {

    suspend fun loginAndSaveSession(username: String, password: String): Session {
        // Prepare initial parameters
        val params = mutableMapOf(
            "method" to "auth.getMobileSession",
            "username" to username,
            "password" to password,
            "api_key" to Constants.API_KEY,
            "format" to "json"
        )

        // Generate Signature
        // Filter out 'format' for signature generation as per Last.fm specs
        val signatureParams = params.filter { it.key != "format" }
        val apiSig = CryptUtils.generateApiSig(signatureParams, Constants.SHARED_SECRET)

        // Add signature to the request params
        params["api_sig"] = apiSig

        val response = lastFmApi.getMobileSession(params)
        val session =
            response.session ?: throw Exception(response.message ?: "Last.fm Login Failed")

        // Save to DataStore (This is now a suspend call)
        sessionManager.saveSession(session.name, session.key)

        // Check & Sync Supabase Profile
        checkAndSyncUserProfile(session.name)

        return session
    }

    private suspend fun checkAndSyncUserProfile(username: String) {
        try {
            val supabase = SupabaseClient.client
            // Check if user exists in DB
            Log.d("DEBUG", "Checking if User already exists in Database")
            val existingUser = supabase.from("user_profiles")
                .select(columns = Columns.list("username")) {
                    filter { eq("username", username) }
                }
                .decodeSingleOrNull<JsonObject>()

            // B. If user does NOT exist, fetch from API and Insert
            if (existingUser == null) {
                Log.d("DEBUG", "User does not exist in Supabase, fetching from API")
                upsertUserFromApi(username)
            }

        } catch (e: Exception) {
            // Log error, but don't crash the login.
            e.printStackTrace()
        }
    }

    suspend fun upsertUserFromApi(username: String) {
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
        SupabaseClient.client.from("user_profiles").upsert(profile) {
            onConflict = "username" // Matches based on Primary Key
        }
    }

    suspend fun getSavedSession(): Session? {
        val key = sessionManager.getSessionKey()
        val username = sessionManager.getUsername()
        if (key != null && username != null) {
            // Reconstruct the Session object from local storage
            return Session(name = username, key = key, subscriber = 0)
        }
        return null
    }

    // Logout
    suspend fun logout() {
        sessionManager.clearSession()
    }
}

