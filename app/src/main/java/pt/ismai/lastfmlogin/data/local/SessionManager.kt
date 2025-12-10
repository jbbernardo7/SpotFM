package pt.ismai.lastfmlogin.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_SESSION = stringPreferencesKey("session_key")
    }

    suspend fun saveSession(username: String, key: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USERNAME] = username
            preferences[KEY_SESSION] = key
        }
    }

    suspend fun getSessionKey(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_SESSION]
    }

    suspend fun getUsername(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[KEY_USERNAME]
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}