package pt.ismai.lastfmlogin.di

import android.content.Context
import pt.ismai.lastfmlogin.data.local.SessionManager
import pt.ismai.lastfmlogin.data.network.RetrofitClient
import pt.ismai.lastfmlogin.data.network.Supabase
import pt.ismai.lastfmlogin.data.repository.AuthRepository
import pt.ismai.lastfmlogin.data.repository.UserRepository

interface AppContainer {
    val authRepository: AuthRepository
    val userRepository: UserRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val sessionManager = SessionManager(context)
    private val lastFmApi = RetrofitClient.api
    private val supabaseClient = Supabase.client

    override val authRepository by lazy {
        AuthRepository(lastFmApi, sessionManager)
    }

    override val userRepository by lazy {
        UserRepository(lastFmApi, supabaseClient)
    }
}