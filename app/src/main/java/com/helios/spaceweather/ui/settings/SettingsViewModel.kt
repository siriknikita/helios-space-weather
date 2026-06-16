package com.helios.spaceweather.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** MVI state/intent for the settings screen. */
data class SettingsState(
    val selectedLanguage: AppLanguage = AppLanguage.current(),
)

sealed interface SettingsIntent {
    data class SelectLanguage(val language: AppLanguage) : SettingsIntent
}

/**
 * Settings ViewModel.
 *
 * Language switching goes through `AppCompatDelegate.setApplicationLocales` — the official
 * per-app language API (Android 13+ system-backed, with AppCompat compatibility below). Setting
 * it triggers a configuration change so the whole UI (and any later notification) re-resolves
 * strings for the new locale; no Context wrapping or manual Activity restart.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SelectLanguage -> applyLanguage(intent.language)
        }
    }

    private fun applyLanguage(language: AppLanguage) {
        if (language == _state.value.selectedLanguage) return
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        _state.update { it.copy(selectedLanguage = language) }
    }
}
