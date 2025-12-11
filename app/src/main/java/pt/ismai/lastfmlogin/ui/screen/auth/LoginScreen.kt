package pt.ismai.lastfmlogin.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import pt.ismai.lastfmlogin.ui.viewmodel.AuthState
import pt.ismai.lastfmlogin.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val state = viewModel.loginState

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (state is AuthState.Authenticating || state is AuthState.Authenticated) {
            CircularProgressIndicator()
        } else {
            val errorMessage = (state as? AuthState.Error)?.message
            LoginForm(
                username = viewModel.username,
                password = viewModel.password,
                errorMessage = errorMessage,
                onUsernameChange = { viewModel.username = it },
                onPasswordChange = { viewModel.password = it },
                onLoginClick = { viewModel.login() }
            )
        }
    }
}

@Composable
fun LoginForm(
    username: String,
    password: String,
    errorMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Last.fm Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username") },
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(0.4f),
            enabled = username.isNotBlank() && password.isNotBlank()
        ) {
            Text("Log In")
        }

        if (errorMessage != null) {
            Text(text = "Error: $errorMessage", color = Color.Red)
        }
    }
}
