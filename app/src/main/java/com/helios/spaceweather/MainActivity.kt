package com.helios.spaceweather

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.helios.spaceweather.core.theme.HeliosTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the Compose UI.
 *
 * Extends [AppCompatActivity] (not ComponentActivity) so the app can use
 * `AppCompatDelegate.setApplicationLocales` for runtime per-app language switching with
 * backward compatibility below Android 13. The full themed navigation graph is introduced
 * in later changes; this scaffold renders a minimal dark placeholder so the project builds
 * and runs end-to-end from the first commit.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            HeliosScaffoldPlaceholder()
        }
    }
}

@Composable
private fun HeliosScaffoldPlaceholder() {
    HeliosTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Text(text = "Helios")
        }
    }
}
