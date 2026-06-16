package com.helios.spaceweather

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.helios.spaceweather.core.theme.HeliosTheme
import com.helios.spaceweather.core.theme.TrueBlack
import com.helios.spaceweather.notification.RequestNotificationPermission
import com.helios.spaceweather.ui.navigation.HeliosNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the Compose UI.
 *
 * Extends [AppCompatActivity] (not ComponentActivity) so the app can use
 * `AppCompatDelegate.setApplicationLocales` for runtime per-app language switching with
 * backward compatibility below Android 13.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            HeliosApp()
        }
    }
}

@Composable
private fun HeliosApp() {
    HeliosTheme {
        // Ask for POST_NOTIFICATIONS once (Android 13+); no-op below 33.
        RequestNotificationPermission()
        Surface(modifier = Modifier.fillMaxSize(), color = TrueBlack) {
            HeliosNavHost()
        }
    }
}
