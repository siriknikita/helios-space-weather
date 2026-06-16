package com.helios.spaceweather.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.helios.spaceweather.BuildConfig
import com.helios.spaceweather.R
import com.helios.spaceweather.core.theme.OnSurfaceMuted
import com.helios.spaceweather.core.theme.TrueBlack
import com.helios.spaceweather.core.util.rememberHeliosHaptics

/**
 * Settings — the language switcher (and a small About block).
 *
 * Selecting a language calls through the ViewModel to `AppCompatDelegate.setApplicationLocales`;
 * the resulting configuration change re-renders the entire app in the new locale automatically,
 * which is exactly why the screen holds no manually-localized state of its own.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptics = rememberHeliosHaptics()

    Scaffold(
        modifier = modifier,
        containerColor = TrueBlack,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TrueBlack),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            SectionHeader(text = stringResource(R.string.settings_language_header))
            Text(
                text = stringResource(R.string.settings_language_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMuted,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            val languages = AppLanguage.entries
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                languages.forEachIndexed { index, language ->
                    SegmentedButton(
                        selected = state.selectedLanguage == language,
                        onClick = {
                            haptics.toggle()
                            viewModel.onIntent(SettingsIntent.SelectLanguage(language))
                        },
                        shape = SegmentedButtonDefaults.itemShape(index, languages.size),
                    ) {
                        Text(stringResource(language.labelRes))
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            SectionHeader(text = stringResource(R.string.settings_about_header))
            Text(
                text = stringResource(R.string.settings_about_body),
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMuted,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = OnSurfaceMuted,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}
