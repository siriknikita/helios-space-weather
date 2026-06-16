package com.helios.spaceweather.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.helios.spaceweather.MainActivity
import com.helios.spaceweather.R
import com.helios.spaceweather.core.util.KpThreatLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds and posts the geomagnetic-storm notification.
 *
 * Crucially, the copy is resolved against the **active per-app locale**, not the device
 * locale. A sync worker runs in the background with no Activity, so its `Context` would
 * otherwise localize against the system language; [localizedContext] rebuilds a Context for
 * the locale the user picked via `AppCompatDelegate.setApplicationLocales`, so a Ukrainian
 * user gets a Ukrainian alert even when the phone is set to English.
 */
@Singleton
class StormNotifier @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {

    /** Create (or update) the storm-alert channel. Idempotent; safe to call repeatedly. */
    fun ensureChannel() {
        val ctx = localizedContext()
        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_HIGH,
        )
            .setName(ctx.getString(R.string.storm_channel_name))
            .setDescription(ctx.getString(R.string.storm_channel_desc))
            .setLightsEnabled(true)
            .setVibrationEnabled(true)
            .build()
        NotificationManagerCompat.from(appContext).createNotificationChannel(channel)
    }

    /** Post a storm alert for [kp]. No-ops if notifications are disabled/revoked. */
    fun notifyStorm(kp: Double) {
        ensureChannel()
        val manager = NotificationManagerCompat.from(appContext)
        if (!manager.areNotificationsEnabled()) return

        val ctx = localizedContext()
        val level = KpThreatLevel.fromKp(kp)
        val kpText = formatKp(kp, ctx.resources.configuration.locales[0])
        val levelLabel = ctx.getString(level.labelRes)

        val title = ctx.getString(R.string.storm_notification_title)
        val text = ctx.getString(R.string.storm_notification_text, kpText, levelLabel)
        val detail = ctx.getString(level.descriptionRes)

        val contentIntent = PendingIntent.getActivity(
            appContext,
            /* requestCode = */ 0,
            Intent(appContext, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_storm)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$text\n\n$detail"))
            .setColor(level.accent.toArgb())
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS revoked between the check and the post — nothing to do.
        }
    }

    /** A Context whose resources resolve against the user-selected app locale. */
    private fun localizedContext(): Context {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return appContext
        val config = Configuration(appContext.resources.configuration)
        config.setLocales(LocaleList.forLanguageTags(locales.toLanguageTags()))
        return appContext.createConfigurationContext(config)
    }

    private fun formatKp(kp: Double, locale: Locale): String = String.format(locale, "%.1f", kp)

    companion object {
        const val CHANNEL_ID = "helios_storm_alerts"
        private const val NOTIFICATION_ID = 1001
    }
}
