package com.helios.spaceweather.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.helios.spaceweather.core.util.KpThreatLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Process-wide DataStore for the notification anti-spam state. */
private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = NotificationPreferences.STORE_NAME,
)

/**
 * Persists the last storm alert that fired and encapsulates the **anti-spam decision**.
 *
 * A new alert is allowed only when the current Kp is at storm level (≥ 5) AND either:
 *  - the last alert was more than [ANTI_SPAM_WINDOW_MILLIS] (12h) ago, OR
 *  - the storm has **escalated** beyond the Kp we last alerted for.
 *
 * This keeps the user informed when things get worse while never re-pinging them for the same
 * (or weaker) storm within the cool-down window.
 */
@Singleton
class NotificationPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.notificationDataStore

    private object Keys {
        val LAST_KP = doublePreferencesKey("last_notified_kp")
        val LAST_AT = longPreferencesKey("last_notified_at_millis")
    }

    /**
     * Decide whether a storm alert should fire for [currentKp] at [nowMillis].
     * Pure read against persisted state; does not record anything.
     */
    suspend fun shouldNotify(currentKp: Double, nowMillis: Long): Boolean {
        if (currentKp < KpThreatLevel.STORM_THRESHOLD) return false
        val prefs = dataStore.data.first()
        val lastKp = prefs[Keys.LAST_KP] ?: 0.0
        val lastAt = prefs[Keys.LAST_AT] ?: 0L

        val outsideCoolDown = nowMillis - lastAt >= ANTI_SPAM_WINDOW_MILLIS
        val escalated = currentKp > lastKp
        return outsideCoolDown || escalated
    }

    /** Record that an alert fired for [kp] at [nowMillis]. */
    suspend fun recordNotified(kp: Double, nowMillis: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_KP] = kp
            prefs[Keys.LAST_AT] = nowMillis
        }
    }

    companion object {
        const val STORE_NAME = "helios_notification_prefs"
        val ANTI_SPAM_WINDOW_MILLIS: Long = TimeUnit.HOURS.toMillis(12)
    }
}
