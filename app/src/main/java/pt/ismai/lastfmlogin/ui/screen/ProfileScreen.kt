package pt.ismai.lastfmlogin.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import pt.ismai.lastfmlogin.data.model.UserProfile
import pt.ismai.lastfmlogin.ui.viewmodel.ProfileState
import pt.ismai.lastfmlogin.ui.viewmodel.ProfileViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import pt.ismai.lastfmlogin.LocalAppContainer
import pt.ismai.lastfmlogin.R
import pt.ismai.lastfmlogin.data.model.Scrobble
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    username: String,
    showLogoutButton : Boolean,
    onLogout: () -> Unit // Get VM instance automatically
) {
    val appContainer = LocalAppContainer.current

    // 2. Create ViewModel using the clean DSL
    val viewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                ProfileViewModel(appContainer.userRepository)
            }
        }
    )

    val state = viewModel.state

    var showEditBioDialog by remember { mutableStateOf(false) }

    // Fetch data when the screen opens
    LaunchedEffect(username) {
        viewModel.fetchProfile(username)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is ProfileState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is ProfileState.Error -> {
                Text("Failed to load profile", modifier = Modifier.align(Alignment.Center))
            }
            is ProfileState.Success -> {
                ProfileContent(
                    profile = state.profile,
                    scrobbles = state.scrobbles,
                    showLogoutButton = showLogoutButton,
                    onLogout = onLogout,
                    onRefresh = { viewModel.refreshProfile(state.profile.username) },
                    onEditBioClick = { showEditBioDialog = true }
                )
                if (showEditBioDialog) {
                    EditBioDialog(
                        currentBio = state.profile.bio ?: "",
                        onDismiss = { showEditBioDialog = false },
                        onSave = { newBio ->
                            viewModel.updateBio(state.profile.username, newBio)
                            showEditBioDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    profile: UserProfile,
    scrobbles: List<Scrobble>,
    showLogoutButton: Boolean,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onEditBioClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- SECTION 1: Header (Image + Names) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Circular Image
            AsyncImage(
                model = profile.image_url ?: "https://lastfm.freetls.fastly.net/i/u/avatar170s/818148bf682d429dc215c1705eb27b98.png", // Default placeholder
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray) // Background while loading
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Names Column
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile.username,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(30.dp).padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }


                Text(
                    text = profile.real_name ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (profile.bio.isNullOrBlank()) "No description yet." else profile.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (profile.bio.isNullOrBlank()) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Only show Edit button if it's MY profile
            if (showLogoutButton) {
                IconButton(onClick = onEditBioClick) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Edit Bio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        // --- SECTION 2: Playcount ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Playcount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${profile.playcount} scrobbles",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        // --- SECTION 3: The List (LazyColumn) ---
        Text(
            text = "Recent History",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Dummy Data List
        val dummyItems = List(20) { "Item ${it + 1}" }

        LazyColumn(
            modifier = Modifier.weight(1f) // Fill remaining space
        ) {
            items(scrobbles) { item ->
                ScrobbleItem(scrobble = item)
                /*Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }*/
            }
        }

        // Logout Button at the bottom
        Spacer(modifier = Modifier.height(8.dp))
        if (showLogoutButton)
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Log Out")
        }
    }
}

@Composable
fun ScrobbleItem(scrobble: Scrobble) {
    // Logic to detect "Now Playing"
    val isNowPlaying = scrobble.date_uts == Long.MAX_VALUE

    val cardBackgroundColor = if (isNowPlaying) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    // Optional: Change text color slightly if the background is different
    val textColor = if (isNowPlaying) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art
            AsyncImage(
                model = scrobble.album_image ?: "https://lastfm.freetls.fastly.net/i/u/300x300/2a96cbd8b46e442fc41c2b86b821562f.png",
                contentDescription = "Album Art",
                modifier = Modifier
                    .size(50.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.DarkGray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scrobble.track_name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = scrobble.artist_name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            // Timestamp or Now Playing Icon
            if (isNowPlaying) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_equalizer), // Or use a specialized icon like "GraphicEq"
                    contentDescription = "Now Playing",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = formatScrobbleDate(scrobble.date_uts),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EditBioDialog(
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Description") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Tell us about yourself") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper to format timestamp
fun formatScrobbleDate(uts: Long): String {
    if (uts == 0L) return ""
    val date = Date(uts * 1000) // Convert seconds to millis
    val format = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return format.format(date)
}