package com.danilkinkin.buckwheat

import android.app.Application
import com.google.android.material.color.DynamicColors

class BuckwheatApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply dynamic color
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
