package com.danilkinkin.buckwheat

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.danilkinkin.buckwheat.subscriptions.SubscriptionCheckWorker
import com.danilkinkin.buckwheat.widget.extend.ExtendWidgetReceiver
import com.danilkinkin.buckwheat.widget.minimal.MinimalWidgetReceiver
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        scheduleSubscriptionChecks()

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

    /**
     * One daily check; WorkManager persists it across reboots. The first run is shifted to the
     * next 10:00 local so a subscription due today does not fire in the middle of the night.
     */
    private fun scheduleSubscriptionChecks() {
        val request = PeriodicWorkRequestBuilder<SubscriptionCheckWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntil(10), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "subscription-daily-check",
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    private fun millisUntil(hourOfDay: Int): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }

        return next.timeInMillis - now.timeInMillis
    }
}
