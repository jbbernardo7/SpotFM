package pt.ismai.lastfmlogin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pt.ismai.lastfmlogin.ui.screen.MainScreen
import pt.ismai.lastfmlogin.ui.screen.auth.LoginScreen
import pt.ismai.lastfmlogin.ui.screen.auth.SplashScreen
import pt.ismai.lastfmlogin.ui.viewmodel.AuthState
import pt.ismai.lastfmlogin.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    val loginState = viewModel.loginState

    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthState.Authenticated -> {
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {  }
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen()
        }

        composable("login") {
            LoginScreen(viewModel)
        }

        composable("home") {
            val username = (loginState as? AuthState.Authenticated)?.session?.name ?: ""
            MainScreen(username = username, onLogout = { viewModel.logout() })
        }
    }


}