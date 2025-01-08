package com.luna.dollargrain

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.luna.dollargrain.widget.extend.ExtendWidgetReceiver
import com.luna.dollargrain.widget.minimal.MinimalWidgetReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {
                ExtendWidgetReceiver.requestUpdateData(activity.applicationContext)
                MinimalWidgetReceiver.requestUpdateData(activity.applicationContext)
            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })
    }
}
