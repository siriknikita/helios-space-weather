package com.helios.spaceweather

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
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
 * Notification-channel creation and periodic-sync scheduling are wired in a later change.
 */
@HiltAndroidApp
class HeliosApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
