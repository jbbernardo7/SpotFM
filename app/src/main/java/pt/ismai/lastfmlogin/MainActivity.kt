package pt.ismai.lastfmlogin

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pt.ismai.lastfmlogin.data.local.SessionManager
import pt.ismai.lastfmlogin.data.network.RetrofitClient
import pt.ismai.lastfmlogin.data.repository.AuthRepository
import pt.ismai.lastfmlogin.di.AppContainer
import pt.ismai.lastfmlogin.di.DefaultAppContainer
import pt.ismai.lastfmlogin.ui.theme.LastfmloginTheme
import pt.ismai.lastfmlogin.ui.viewmodel.AuthViewModel

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided")
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as SpotFmApplication).container

        setContent {
            LastfmloginTheme {
                CompositionLocalProvider(LocalAppContainer provides appContainer) {
                    val viewModel: AuthViewModel by viewModels {
                        viewModelFactory {
                            initializer { AuthViewModel(appContainer.authRepository) }
                        }
                    }

                    AppNavigation(viewModel)
                }
            }
        }
    }
}

class SpotFmApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}