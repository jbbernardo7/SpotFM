package pt.ismai.lastfmlogin.ui.screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
//import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.compose.AsyncImage
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.geometry
import pt.ismai.lastfmlogin.LocalAppContainer
import pt.ismai.lastfmlogin.R
import pt.ismai.lastfmlogin.data.model.Scrobble
import pt.ismai.lastfmlogin.data.model.UserProfile
import pt.ismai.lastfmlogin.ui.viewmodel.MapViewModel

@Composable
fun MapScreen(
    username: String,
    onUserClick: (String) -> Unit // Callback to open Profile
) {
    val appContainer = LocalAppContainer.current

    val viewModel: MapViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                MapViewModel(appContainer.userRepository, appContainer.locationRepository)
            }
        }
    )
    val state = viewModel.state
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }
    var selectedUserSong by remember { mutableStateOf<Scrobble?>(null) }

    // Map Viewport (Camera)
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(14.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.initialize(username)
            }
        }
    )

    // Check permissions on start
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Sync Camera to User Location once found
    LaunchedEffect(state.userLocation) {
        state.userLocation?.let { point ->
            mapViewportState.flyTo(
                CameraOptions.Builder().center(point).zoom(15.0).build()
            )
        }
    }

    LaunchedEffect(selectedUser) {
        if (selectedUser != null) {
            selectedUserSong = viewModel.getLastTrack(selectedUser!!.username)
        } else {
            selectedUserSong = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. THE MAP
        MapboxMap(
            Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            style = { MapStyle(style = Style.DARK) },
            onMapClickListener = {
                selectedUser = null
                false
            }// or Style.MAPBOX_STREETS
        ) {
            // A. Show Other Users as Pins
            state.nearbyUsers.forEach { user ->
                if (user.latitude != null && user.longitude != null) {
                    ViewAnnotation(
                        options = com.mapbox.maps.viewannotation.viewAnnotationOptions {
                            geometry(Point.fromLngLat(user.longitude, user.latitude))
                            allowOverlap(true)
                        }
                    ) {
                        UserMapPin(
                            imageUrl = user.image_url,
                            username = user.username,
                            //onClick = { onUserClick(user.username) }
                            onClick = { selectedUser = user }
                        )
                    }
                }
            }

            // B. Show ME (The Blue Puck)
            state.userLocation?.let { myPoint ->
                ViewAnnotation(
                    options = com.mapbox.maps.viewannotation.viewAnnotationOptions {
                        geometry(myPoint)
                        allowOverlap(true) // Ensure it draws even if close to others
                    }
                ) {
                    MyLocationDot()
                }
            }
        }

        // 2. UI OVERLAYS (Buttons & Switches)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            // Ghost Mode Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Visible", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = true,
                    onCheckedChange = null,
                    //checked = state.isVisibleOnMap,
                    //onCheckedChange = { viewModel.toggleVisibility(username, it) },
                    modifier = Modifier.scale(0.8f)
                )
            }
        }

        // Recenter Button
        FloatingActionButton(
            onClick = { viewModel.getCurrentLocation(username) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Recenter")
        }

        AnimatedVisibility(
            visible = selectedUser != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedUser?.let { user ->
                UserInfoCard(
                    user = user,
                    lastTrack = selectedUserSong,
                    onClose = { selectedUser = null },
                    onViewProfile = { onUserClick(user.username) }
                )
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun MyLocationDot() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(24.dp)
    ) {
        // Outer Glow (Optional translucent circle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
        )
        // Inner Solid Dot
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

// Custom Pin Composable
@Composable
fun UserMapPin(
    imageUrl: String?,
    username: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(bottom = 12.dp) // Lift pin slightly above exact point
    ) {
        // Avatar
        AsyncImage(
            model = imageUrl ?: "https://lastfm.freetls.fastly.net/i/u/avatar170s/818148bf682d429dc215c1705eb27b98.png",
            contentDescription = username,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        // Little Arrow/Triangle (Optional, using a Box for simplicity)
        Box(
            modifier = Modifier
                .size(width = 8.dp, height = 8.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
        )
    }
}

@Composable
fun UserInfoCard(
    user: UserProfile,
    lastTrack: Scrobble?,
    onClose: () -> Unit,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(120.dp)
            .clickable { onViewProfile() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            AsyncImage(
                model = user.image_url ?: "https://lastfm.freetls.fastly.net/i/u/avatar170s/818148bf682d429dc215c1705eb27b98.png",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // 1. Username
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Song Info (Dynamic)
                if (lastTrack != null) {
                    val isNowPlaying = lastTrack.date_uts == Long.MAX_VALUE

                    if (isNowPlaying) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Equalizer Icon or simply text
                            Icon(
                                painter = painterResource(id = R.drawable.ic_equalizer), // Ensure you have this icon or use R.drawable.ic_equalizer
                                contentDescription = "Listening",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Listening now...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = lastTrack.track_name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = lastTrack.artist_name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Done, // Or generic icon
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "No recent scrobbles",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            // Close Button (Top Right of card)
            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.Top)) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}

/*package pt.ismai.lastfmlogin.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle

@Composable
fun MapScreen() {
    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = rememberMapViewportState {
            setCameraOptions {
                zoom(16.4)
                center(Point.fromLngLat(-8.61652, 41.26942))
                pitch(0.0)
                bearing(0.0)
            }
        },
        style = { MapStyle(style = "mapbox://styles/mapbox/dark-v11") }
    )
}*/