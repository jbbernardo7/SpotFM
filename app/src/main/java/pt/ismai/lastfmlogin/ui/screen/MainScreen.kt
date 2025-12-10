package pt.ismai.lastfmlogin.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import pt.ismai.lastfmlogin.ui.screen.navbar.BottomNavItem
import pt.ismai.lastfmlogin.ui.screen.navbar.BottomNavigationBar
import pt.ismai.lastfmlogin.ui.screen.navbar.HomeScreen
import pt.ismai.lastfmlogin.ui.screen.navbar.ProfileScreen

@Composable
fun MainScreen(username: String, onLogout: () -> Unit) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            // We moved the logic to a helper function to keep this file clean
            BottomNavigationBar(navController = bottomNavController)
        }
    ) { innerPadding ->

        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // TAB 1: Home Feed (Renamed from HomeContent)
            composable(BottomNavItem.Home.route) {
                HomeScreen(username)
            }

            // TAB 2: Map
            composable(BottomNavItem.Map.route) {
                //MapScreen()
            }

            // TAB 3: Events
            composable(BottomNavItem.Events.route) {
                //EventsScreen()
            }

            // TAB 4: Profile
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(username = username, onLogout = onLogout)
            }
        }
    }
}
