package pt.ismai.lastfmlogin.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import pt.ismai.lastfmlogin.ui.components.BottomNavItem
import pt.ismai.lastfmlogin.ui.components.BottomNavigationBar

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

            composable("map") {
                MapScreen(
                    username = username, // Pass current user for location tracking
                    onUserClick = { clickedUsername ->
                        println("Clicked user: $clickedUsername")
                        // Navigate to the "View Profile" route with the clicked username
                        //bottomNavController.navigate("profile_view/$clickedUsername")
                    }
                )
            }

            // TAB 3: Events
            composable(BottomNavItem.Events.route) {
                //
            }

            // TAB 4: Profile
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(username = username, showLogoutButton = true, onLogout = onLogout)
            }
        }
    }
}
