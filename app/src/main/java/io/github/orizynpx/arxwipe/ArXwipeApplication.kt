package io.github.orizynpx.arxwipe

import android.app.Application
import com.google.android.material.color.DynamicColors

class ArXwipeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}