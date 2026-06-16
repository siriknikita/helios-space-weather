package com.helios.spaceweather.core.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Thin wrapper over the platform's view-level haptics so the UI can request *subtle*,
 * semantically-named feedback (a scrub tick, a toggle confirm) without each call site
 * reaching for raw [HapticFeedbackConstants].
 *
 * View-level constants are used rather than Compose's [androidx.compose.ui.hapticfeedback]
 * because they expose the lighter `CLOCK_TICK`/`CONTEXT_CLICK` effects that suit a timeline
 * scrub far better than the heavier `LongPress`.
 */
class HeliosHaptics(private val view: View) {

    /** A faint tick — used while scrubbing the timeline across data points. */
    fun tick() {
        view.performHapticFeedback(
            HapticFeedbackConstants.CLOCK_TICK,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING,
        )
    }

    /** A slightly firmer confirm — used when toggling a setting (e.g. language). */
    fun toggle() {
        view.performHapticFeedback(
            HapticFeedbackConstants.CONTEXT_CLICK,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING,
        )
    }
}

/** Remember a [HeliosHaptics] bound to the current composition's host [View]. */
@Composable
fun rememberHeliosHaptics(): HeliosHaptics {
    val view = LocalView.current
    return remember(view) { HeliosHaptics(view) }
}
