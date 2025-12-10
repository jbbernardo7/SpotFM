package pt.ismai.lastfmlogin.ui.screen.navbar

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
import coil3.compose.AsyncImage
import pt.ismai.lastfmlogin.ui.viewmodel.ProfileState
import pt.ismai.lastfmlogin.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    username: String,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel() // Get VM instance automatically
) {
    val state = viewModel.state

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
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    profile: pt.ismai.lastfmlogin.data.model.UserProfile,
    onLogout: () -> Unit
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
                Text(
                    text = profile.username,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = profile.real_name ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
            items(dummyItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Logout Button at the bottom
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Log Out")
        }
    }
}