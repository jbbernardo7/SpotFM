package pt.ismai.lastfmlogin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pt.ismai.lastfmlogin.data.local.SessionManager
import pt.ismai.lastfmlogin.data.network.RetrofitClient
import pt.ismai.lastfmlogin.data.repository.AuthRepository
import pt.ismai.lastfmlogin.di.LocalAuthRepository
import pt.ismai.lastfmlogin.ui.theme.LastfmloginTheme
import pt.ismai.lastfmlogin.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    private val sessionManager by lazy { SessionManager(applicationContext) }
    private val repository by lazy { AuthRepository(RetrofitClient.api, sessionManager) }
    private val viewModel: AuthViewModel by viewModels {
        viewModelFactory {
            initializer { AuthViewModel(repository) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LastfmloginTheme {
                CompositionLocalProvider(LocalAuthRepository provides repository) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LastfmloginTheme {
        Greeting("Android")
    }
}