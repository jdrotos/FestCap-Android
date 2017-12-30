package org.jdrotos.festcap

import android.app.Application
import timber.log.Timber

/**
 * Created by jdrotos on 11/18/17.
 */
class FestCapApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}