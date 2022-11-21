package com.danilkinkin.buckwheat.util

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.danilkinkin.buckwheat.BuildConfig
import com.danilkinkin.buckwheat.dataStore
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

fun initSentry(context: Context) {
    val currentValue = runBlocking { context.dataStore.data.first() }

    val sendCrashReport = (currentValue[stringPreferencesKey("send_crash_report")] ?: "false").toBoolean()

    if (!sendCrashReport || BuildConfig.SENTRY_DSN.isEmpty()) {
        Log.d("initSentry", "Disable crash report. Ignore sentry")
        return
    }

    Log.d("initSentry", "Init sentry...")

    SentryAndroid.init(context) { options ->
        options.dsn = BuildConfig.SENTRY_DSN
        options.sampleRate = 0.2
        options.isEnableUserInteractionTracing = true
        options.isEnableUserInteractionBreadcrumbs = true

        options.beforeSend = SentryOptions.BeforeSendCallback { event, hint ->
            if (SentryLevel.DEBUG == event.level) {
                null
            } else {
                event
            }
        }
    }
}

suspend fun switchIsSendCrashReport(context: Context, enabled: Boolean) {
    context.dataStore.edit {
        it[stringPreferencesKey("send_crash_report")] = enabled.toString()
    }
}