package com.helios.spaceweather.data.datastore

import com.helios.spaceweather.data.datastore.NotificationPreferences.Companion.decide
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Exhaustively pins the storm-alert anti-spam rule: fire only at Kp ≥ 5 AND (outside the 12h
 * cool-down OR escalated beyond the last alert). These cases are the contract that keeps the
 * app from either spamming the user or going silent when a storm worsens.
 */
class AntiSpamDecisionTest {

    private val window = TimeUnit.HOURS.toMillis(12)
    private val now = TimeUnit.DAYS.toMillis(100) // an arbitrary "now" well past epoch

    @Test
    fun `below storm threshold never notifies`() {
        assertFalse(decide(currentKp = 4.99, lastNotifiedKp = 0.0, lastNotifiedAtMillis = 0L, nowMillis = now))
    }

    @Test
    fun `first storm with no prior alert notifies`() {
        assertTrue(decide(currentKp = 5.0, lastNotifiedKp = 0.0, lastNotifiedAtMillis = 0L, nowMillis = now))
    }

    @Test
    fun `same storm within cool-down is suppressed`() {
        val oneHourAgo = now - TimeUnit.HOURS.toMillis(1)
        assertFalse(
            decide(currentKp = 5.0, lastNotifiedKp = 5.0, lastNotifiedAtMillis = oneHourAgo, nowMillis = now),
        )
    }

    @Test
    fun `escalation within cool-down still notifies`() {
        val oneHourAgo = now - TimeUnit.HOURS.toMillis(1)
        assertTrue(
            decide(currentKp = 7.0, lastNotifiedKp = 5.0, lastNotifiedAtMillis = oneHourAgo, nowMillis = now),
        )
    }

    @Test
    fun `same storm after cool-down notifies again`() {
        val thirteenHoursAgo = now - TimeUnit.HOURS.toMillis(13)
        assertTrue(
            decide(currentKp = 5.0, lastNotifiedKp = 5.0, lastNotifiedAtMillis = thirteenHoursAgo, nowMillis = now),
        )
    }

    @Test
    fun `weaker storm within cool-down is suppressed`() {
        val oneHourAgo = now - TimeUnit.HOURS.toMillis(1)
        assertFalse(
            decide(currentKp = 5.0, lastNotifiedKp = 7.0, lastNotifiedAtMillis = oneHourAgo, nowMillis = now),
        )
    }

    @Test
    fun `cool-down boundary is inclusive`() {
        val exactlyWindowAgo = now - window
        assertTrue(
            decide(currentKp = 5.0, lastNotifiedKp = 5.0, lastNotifiedAtMillis = exactlyWindowAgo, nowMillis = now),
        )
    }
}
