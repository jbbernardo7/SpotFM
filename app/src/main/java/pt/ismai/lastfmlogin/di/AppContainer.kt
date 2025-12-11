package pt.ismai.lastfmlogin.di

import android.content.Context
import pt.ismai.lastfmlogin.data.local.SessionManager
import pt.ismai.lastfmlogin.data.network.LastFmApi
import pt.ismai.lastfmlogin.data.network.RetrofitClient
import pt.ismai.lastfmlogin.data.network.SupabaseClient
import pt.ismai.lastfmlogin.data.repository.AuthRepository

interface AppContainer {
    val authRepository: AuthRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val sessionManager = SessionManager(context)

    override val authRepository by lazy {
        AuthRepository(RetrofitClient.api, sessionManager)
    }
}