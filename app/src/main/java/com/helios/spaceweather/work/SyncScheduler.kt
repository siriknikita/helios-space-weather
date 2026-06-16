package com.helios.spaceweather.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules the recurring [KpSyncWorker]. Idempotent: KEEP means re-enqueuing on every app
 * start never resets an already-running schedule, while still re-arming it after a reboot or
 * an app update wiped the queue.
 */
object SyncScheduler {

    /** Poll NOAA every 30 minutes while a network is available. */
    private const val SYNC_INTERVAL_MINUTES = 30L
    private const val BACKOFF_DELAY_MINUTES = 10L

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<KpSyncWorker>(
            SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES,
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY_MINUTES, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            KpSyncWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
