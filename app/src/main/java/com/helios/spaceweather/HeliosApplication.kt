package com.helios.spaceweather

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.helios.spaceweather.notification.StormNotifier
import com.helios.spaceweather.work.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point.
 *
 * Annotated with [HiltAndroidApp] to bootstrap the Hilt dependency graph, and implements
 * [Configuration.Provider] so WorkManager is initialized with Hilt's [HiltWorkerFactory]
 * (the default WorkManager initializer is removed in the manifest). This lets background
 * workers receive constructor-injected dependencies.
 *
 * On startup it also creates the storm notification channel (so it appears in system settings
 * before the first alert) and schedules the 30-minute background sync (KEEP, so this is safe
 * on every launch and re-arms the worker after a reboot/update).
 */
@HiltAndroidApp
class HeliosApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var stormNotifier: StormNotifier

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        stormNotifier.ensureChannel()
        SyncScheduler.schedule(this)
    }
}
