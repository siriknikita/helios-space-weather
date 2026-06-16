package com.helios.spaceweather.domain.model

import com.helios.spaceweather.core.util.KpThreatLevel
import java.time.Instant

/**
 * A single planetary Kp index data point.
 *
 * @param time the UTC instant the reading applies to (3-hour cadence for NOAA Kp).
 * @param kp the planetary K index, 0.0–9.0.
 * @param isForecast true for predicted points, false for observed/estimated history.
 */
data class KpReading(
    val time: Instant,
    val kp: Double,
    val isForecast: Boolean,
) {
    /** The interpreted threat level for this reading (color + label source of truth). */
    val threatLevel: KpThreatLevel get() = KpThreatLevel.fromKp(kp)
}
