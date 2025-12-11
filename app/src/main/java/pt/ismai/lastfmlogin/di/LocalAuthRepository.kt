package pt.ismai.lastfmlogin.di

import androidx.compose.runtime.staticCompositionLocalOf
import pt.ismai.lastfmlogin.data.repository.AuthRepository

val LocalAuthRepository = staticCompositionLocalOf<AuthRepository> {
    error("No AuthRepository provided!")
}