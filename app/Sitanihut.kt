package com.dishut_lampung.sitanihut

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SitanihutApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}