package pt.ismai.lastfmlogin.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home_feed", "Home", Icons.Default.Home)
    object Map : BottomNavItem("map", "Map", Icons.Default.LocationOn)
    object Nearby : BottomNavItem("nearby", "Nearby", Icons.Default.Search)
    object Events : BottomNavItem("events", "Events", Icons.Default.DateRange)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}