package dev.hossain.timeline

import android.app.Application
import dev.hossain.timeline.di.AppComponent
import timber.log.Timber

/**
 * Application class for the app with key initializations.
 */
class TimelineApp : Application() {
    private val appComponent: AppComponent by lazy { AppComponent.create(this) }

    fun appComponent(): AppComponent = appComponent

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
