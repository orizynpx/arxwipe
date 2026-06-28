package io.github.orizynpx.arxwipe

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import dagger.hilt.android.HiltAndroidApp
import io.github.orizynpx.arxwipe.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ArxwipeApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var repository: PreferencesRepository

    companion object {
        var isDynamicColorsEnabled: Boolean = false
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        
        Timber.plant(Timber.DebugTree())
        
        
        isDynamicColorsEnabled = runBlocking { repository.isDynamicColorsEnabled().first() }
        
        
        DynamicColors.applyToActivitiesIfAvailable(this, DynamicColorsOptions.Builder()
            .setPrecondition { _, _ -> isDynamicColorsEnabled }
            .build())
        
        Timber.d("Application initialized with dynamic colors: $isDynamicColorsEnabled")
    }
}
