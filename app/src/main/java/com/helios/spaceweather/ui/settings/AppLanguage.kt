package com.helios.spaceweather.ui.settings

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import com.helios.spaceweather.R

/**
 * The languages Helios ships. Labels are endonyms (each language in its own name) and are
 * intentionally not translated, so the segmented control reads the same in any UI locale.
 *
 * @param tag the BCP-47 tag handed to `AppCompatDelegate.setApplicationLocales`.
 */
enum class AppLanguage(
    val tag: String,
    @StringRes val labelRes: Int,
) {
    ENGLISH("en", R.string.language_english),
    UKRAINIAN("uk", R.string.language_ukrainian),
    ;

    companion object {
        /** Resolve the currently-applied app language, defaulting to English. */
        fun current(): AppLanguage {
            val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            return entries.firstOrNull { tags.startsWith(it.tag, ignoreCase = true) } ?: ENGLISH
        }
    }
}
