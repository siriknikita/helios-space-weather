package com.helios.spaceweather.core.util

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.helios.spaceweather.R
import com.helios.spaceweather.core.theme.KpAmber
import com.helios.spaceweather.core.theme.KpCrimson
import com.helios.spaceweather.core.theme.KpGreen

/**
 * The single source of truth for interpreting a planetary Kp index.
 *
 * Maps a Kp value to a human-readable threat level, its localized label/description, the
 * NOAA G-scale designation (international, not translated), and the accent color used to
 * indicate it. The color buckets honor the spec exactly — Green for Kp 0–3, Amber for 4–5,
 * Crimson for 6+ — while the eight levels give the status panel finer language.
 *
 * Every screen and the storm notifier resolve color and copy through here so the mapping
 * can never drift between surfaces.
 */
enum class KpThreatLevel(
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    /** NOAA geomagnetic storm scale, e.g. "G1"; null below storm level. */
    val gScale: String?,
    val accent: Color,
) {
    QUIET(R.string.threat_quiet, R.string.threat_desc_quiet, null, KpGreen),
    UNSETTLED(R.string.threat_unsettled, R.string.threat_desc_unsettled, null, KpGreen),
    ACTIVE(R.string.threat_active, R.string.threat_desc_active, null, KpAmber),
    STORM_MINOR(R.string.threat_storm_minor, R.string.threat_desc_storm_minor, "G1", KpAmber),
    STORM_MODERATE(R.string.threat_storm_moderate, R.string.threat_desc_storm_moderate, "G2", KpCrimson),
    STORM_STRONG(R.string.threat_storm_strong, R.string.threat_desc_storm_strong, "G3", KpCrimson),
    STORM_SEVERE(R.string.threat_storm_severe, R.string.threat_desc_storm_severe, "G4", KpCrimson),
    STORM_EXTREME(R.string.threat_storm_extreme, R.string.threat_desc_storm_extreme, "G5", KpCrimson),
    ;

    /** True once conditions reach NOAA G1 (Kp ≥ 5) — the notification trigger threshold. */
    val isStorm: Boolean get() = ordinal >= STORM_MINOR.ordinal

    companion object {
        /** Kp at/above which a geomagnetic storm alert should fire (NOAA G1). */
        const val STORM_THRESHOLD = 5.0

        /** Maximum value of the Kp scale; used to normalize gauges/charts. */
        const val KP_MAX = 9.0

        /**
         * Resolve a [KpThreatLevel] from a raw Kp value. Values are floored into integer Kp
         * bands (Kp is reported in thirds, e.g. 4.67, but storm scaling is by whole Kp).
         */
        fun fromKp(kp: Double): KpThreatLevel = when {
            kp < 3.0 -> QUIET
            kp < 4.0 -> UNSETTLED
            kp < 5.0 -> ACTIVE
            kp < 6.0 -> STORM_MINOR
            kp < 7.0 -> STORM_MODERATE
            kp < 8.0 -> STORM_STRONG
            kp < 9.0 -> STORM_SEVERE
            else -> STORM_EXTREME
        }
    }
}
