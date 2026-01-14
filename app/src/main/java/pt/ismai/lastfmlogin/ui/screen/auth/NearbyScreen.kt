package pt.ismai.lastfmlogin.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.compose.AsyncImage
import pt.ismai.lastfmlogin.LocalAppContainer
import pt.ismai.lastfmlogin.ui.viewmodel.NearbyState
import pt.ismai.lastfmlogin.ui.viewmodel.NearbyUser
import pt.ismai.lastfmlogin.ui.viewmodel.NearbyViewModel

@Composable
fun NearbyScreen(
    username: String,
    onUserClick: (String) -> Unit
) {
    val appContainer = LocalAppContainer.current
    val viewModel: NearbyViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                NearbyViewModel(appContainer.userRepository, appContainer.locationRepository)
            }
        }
    )
    val state = viewModel.state

    // Permission handling (needed to know MY location to calculate distance)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.loadNearbyUsers(username)
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.loadNearbyUsers(username) }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is NearbyState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NearbyState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Could not determine location.")
                        Button(onClick = { viewModel.loadNearbyUsers(username) }) {
                            Text("Retry")
                        }
                    }
                }
                is NearbyState.Empty -> {
                    Text(
                        text = "No active users found within 10km.",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }
                is NearbyState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = "People Nearby",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        items(state.users) { nearbyUser ->
                            NearbyUserItem(
                                nearbyUser = nearbyUser,
                                onClick = { onUserClick(nearbyUser.user.username) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyUserItem(nearbyUser: NearbyUser, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = {
            AsyncImage(
                model = nearbyUser.user.image_url ?: "https://lastfm.freetls.fastly.net/i/u/avatar170s/818148bf682d429dc215c1705eb27b98.png",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
        },
        headlineContent = {
            Text(nearbyUser.user.username, fontWeight = FontWeight.Bold)
        },
        supportingContent = {
            // Format distance to 1 decimal place (e.g. "3.2 km")
            Text(
                text = "${String.format("%.1f", nearbyUser.distanceKm)} km away",
                color = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(Icons.Default.Person, contentDescription = "View Profile")
        }
    )
}