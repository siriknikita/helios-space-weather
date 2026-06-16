package com.helios.spaceweather.domain.model

import java.time.Instant

/**
 * An immutable view of everything the UI needs to render: the latest observed reading, the
 * observed history, and the forecast — all derived from the local Room cache so the screen
 * is offline-first.
 *
 * @param current the most recent observed reading, or null when the cache is empty.
 * @param history observed readings, ascending by time.
 * @param forecast predicted readings, ascending by time.
 */
data class KpSnapshot(
    val current: KpReading?,
    val history: List<KpReading>,
    val forecast: List<KpReading>,
) {
    /** When the latest observed reading was taken. */
    val lastUpdated: Instant? get() = current?.time

    /** Observed history followed by forecast — the continuous series the timeline draws. */
    val timeline: List<KpReading> get() = history + forecast

    companion object {
        val EMPTY = KpSnapshot(current = null, history = emptyList(), forecast = emptyList())
    }
}
