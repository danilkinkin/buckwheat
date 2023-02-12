package com.danilkinkin.buckwheat

import androidx.compose.runtime.*
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.google.android.glance.tools.viewer.GlanceSnapshot
import com.google.android.glance.tools.viewer.GlanceViewerActivity
import java.util.*

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
class WidgetViewerActivity : GlanceViewerActivity() {

    override suspend fun getGlanceSnapshot(
        receiver: Class<out GlanceAppWidgetReceiver>
    ): GlanceSnapshot {
        return when (receiver) {
            AppWidgetReceiver::class.java -> GlanceSnapshot(
                instance = AppWidget(),
            )
            else -> throw IllegalArgumentException()
        }
    }

    override fun getProviders() = listOf(AppWidgetReceiver::class.java)
}
