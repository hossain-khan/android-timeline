package dev.hossain.timeline

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.MapsInitializer.Renderer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import dev.hossain.timeline.di.AppComponent
import timber.log.Timber

/**
 * Application class for the app with key initializations.
 */
class TimelineApp :
    Application(),
    OnMapsSdkInitializedCallback {
    private val appComponent: AppComponent by lazy { AppComponent.create(this) }

    fun appComponent(): AppComponent = appComponent

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        MapsInitializer.initialize(applicationContext, Renderer.LATEST, this)
    }

    /**
     * Callback when Google Maps SDK is initialized.
     * - https://developers.google.com/maps/documentation/android-sdk/renderer#kotlin
     * - https://developers.google.com/android/reference/com/google/android/gms/maps/MapsInitializer
     */
    override fun onMapsSdkInitialized(renderer: Renderer) {
        when (renderer) {
            Renderer.LATEST -> Timber.d("The latest version of the renderer is used.")
            Renderer.LEGACY -> Timber.d("The legacy version of the renderer is used.")
        }
    }
}
