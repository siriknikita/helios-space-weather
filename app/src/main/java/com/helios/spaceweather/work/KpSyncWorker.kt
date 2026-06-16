package com.helios.spaceweather.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.helios.spaceweather.data.datastore.NotificationPreferences
import com.helios.spaceweather.domain.repository.KpRepository
import com.helios.spaceweather.notification.StormNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodic background sync. Refreshes the Kp cache from NOAA and, when the latest observed
 * reading crosses storm level, fires a (debounced) alert.
 *
 * The alert decision lives in [NotificationPreferences.shouldNotify] (Kp ≥ 5, outside the 12h
 * cool-down or escalated) so this worker stays a thin orchestrator. On network failure it
 * retries with backoff up to [MAX_RETRIES], then succeeds to wait for the next period rather
 * than burning retries indefinitely.
 */
@HiltWorker
class KpSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: KpRepository,
    private val notificationPreferences: NotificationPreferences,
    private val stormNotifier: StormNotifier,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val outcome = repository.refresh()

        return outcome.fold(
            onSuccess = { snapshot ->
                val current = snapshot.current
                if (current != null) {
                    val now = System.currentTimeMillis()
                    if (notificationPreferences.shouldNotify(current.kp, now)) {
                        stormNotifier.notifyStorm(current.kp)
                        notificationPreferences.recordNotified(current.kp, now)
                    }
                }
                Result.success()
            },
            onFailure = {
                if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
            },
        )
    }

    companion object {
        const val UNIQUE_NAME = "helios_kp_periodic_sync"
        private const val MAX_RETRIES = 3
    }
}
