package pt.ismai.lastfmlogin.data.repository

import pt.ismai.lastfmlogin.data.local.SessionManager
import pt.ismai.lastfmlogin.data.model.Session
import pt.ismai.lastfmlogin.data.network.LastFmApi
import pt.ismai.lastfmlogin.utils.Constants
import pt.ismai.lastfmlogin.utils.CryptUtils

class AuthRepository(private val lastFmApi: LastFmApi, private val sessionManager: SessionManager) {

    suspend fun fetchSessionFromLastFm(username: String, password: String): Session {
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
        val session = response.session ?: throw Exception(response.message ?: "Last.fm Login Failed")

        return session
    }

    suspend fun saveSession(session: Session) {
        sessionManager.saveSession(session.name, session.key)
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

