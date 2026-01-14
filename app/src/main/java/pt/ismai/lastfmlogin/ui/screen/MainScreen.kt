package pt.ismai.lastfmlogin.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
                        //println("Clicked user: $clickedUsername")

                        // Navigate to the "View Profile" route with the clicked username
                        bottomNavController.navigate("profile/$clickedUsername")
                    }
                )
            }

            // TAB 3: Nearby
            composable(BottomNavItem.Nearby.route) { // Use your constant
                NearbyScreen(
                    username = username,
                    onUserClick = { clickedUsername ->
                        // Navigate to the dynamic profile route
                        bottomNavController.navigate("profile/$clickedUsername")
                    }
                )
            }

            // TAB 4: Events
            composable(BottomNavItem.Events.route) {
                //
            }

            // TAB 5: Profile
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(username = username, showLogoutButton = true, onLogout = onLogout)
            }

            composable(
                route = "profile/{targetUsername}",
                arguments = listOf(navArgument("targetUsername") { type = NavType.StringType })
            ) { backStackEntry ->
                // 1. Get the username passed in the URL
                val targetUser = backStackEntry.arguments?.getString("targetUsername") ?: return@composable

                // 2. Check if we are looking at ourselves
                val isMe = targetUser.equals(username, ignoreCase = true)

                // 3. Render ProfileScreen with that specific user's data
                ProfileScreen(
                    username = targetUser,
                    showLogoutButton = isMe, // Only show Logout if it's MY profile
                    onLogout = onLogout
                )
            }
        }
    }
}
